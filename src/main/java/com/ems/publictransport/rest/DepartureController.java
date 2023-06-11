package com.ems.publictransport.rest;

import com.ems.publictransport.rest.resource.DepartureData;
import com.ems.publictransport.util.ProviderUtil;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NvbwProvider;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;
import de.schildbach.pte.exception.AbstractHttpException;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Timed
@RestController
@RequestMapping(value = "rest/departure")
public class DepartureController {
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

    private ProviderUtil providerUtil;

    public DepartureController(ProviderUtil providerUtil) {
        this.providerUtil = providerUtil;
    }

    @GetMapping
    public ResponseEntity departure( @RequestParam String from,
                                     @RequestParam(value = "provider", defaultValue = "Nvbw") String providerName,
        @RequestParam(defaultValue = "10") int limit) throws IOException {
        try {
            if(providerName.equalsIgnoreCase("bahn"))
                providerName = "DB";
            NetworkProvider provider = getNetworkProvider(providerName);
            if (provider == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider " + providerName + " not found or can not instantiated...");
            QueryDeparturesResult efaData = provider.queryDepartures(from, new Date(), 120, true);
            if (efaData.status.name().equals("OK")) {
                List<DepartureData> list = new ArrayList<>();
                if (efaData.findStationDepartures(from) == null && !efaData.stationDepartures.isEmpty()) {
                    for (StationDepartures stationDeparture : efaData.stationDepartures) {
                        list.addAll(convertDepartures(stationDeparture));
                    }
                    Collections.sort(list);
                } else
                    list.addAll(convertDepartures(efaData.findStationDepartures(from)));
                if (list.size() > limit)
                    list = list.subList(0, limit);
                return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(list);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("EFA error status: " + efaData.status.name());
        }catch(AbstractHttpException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Called url: " + e.getUrl() + "\r\nResponse: " + e.getBodyPeek());
        }
        catch( SocketTimeoutException | SocketException e){
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Timeout, Provider " + providerName + " not responding in 15 seconds or closed the connection");
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provider [" + providerName + "] not found, please check for new one");
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Can not parse result from provider, message:" +e.getMessage());
        }
    }

    @GetMapping("FHEM")
    public ResponseEntity departureFHEM(@RequestParam String from, @RequestParam(value = "provider", defaultValue = "Nvbw") String providerName,
        @RequestParam(defaultValue = "10") int limit) throws IOException {
        try {
            if(providerName.equalsIgnoreCase("bahn"))
                providerName = "DB";
            NetworkProvider provider = getNetworkProvider(providerName);
            if (provider == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider " + providerName + " not found or can not instantiated...");
            QueryDeparturesResult efaData = provider.queryDepartures(from, new Date(), 120, true);
            if (efaData.status.name().equals("OK")) {
                String data = "";
                if (efaData.findStationDepartures(from) == null && !efaData.stationDepartures.isEmpty()) {
                    List<DepartureData> list = new ArrayList<>();
                    for (StationDepartures stationDeparture : efaData.stationDepartures) {
                        list.addAll(convertDepartures(stationDeparture));
                    }
                    Collections.sort(list);
                    StringBuffer sb = new StringBuffer();
                    sb.append("[");
                    int count = 0;
                    for (DepartureData departureData : list) {
                        sb.append("[\"" + departureData.getNumber() + "\",\"" + departureData.getTo() + "\",\"" + departureData.getDepartureTimeInMinutes() + "\"],");
                        count++;
                        if (count >= limit)
                            break;
                    }
                    String lines = sb.toString();
                    data = lines.substring(0, lines.lastIndexOf(',')) + "]";
                } else
                    data = convertDeparturesFHEM(efaData.findStationDepartures(from), limit);
                if(data == null || data.isEmpty())
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("EFA error status: " + efaData.status.name());
                return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(data);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("EFA error status: " + efaData.status.name());
        }catch(AbstractHttpException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Called url: " + e.getUrl() + "\r\nResponse: " + e.getBodyPeek());
        }catch(SocketTimeoutException | SocketException e){
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Timeout, Provider " + providerName + " not responding in 15 seconds or closed the connection");
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provider [" + providerName + "] not found, please check for new one");
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Can not parse result from provider, message:" +e.getMessage());
        }catch(IOException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Can not parse result or other from provider, message:" +e.getMessage());
        }

    }

    private NetworkProvider getNetworkProvider(String providerName) {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new NvbwProvider();
        return provider;
    }

    private String convertDeparturesFHEM(StationDepartures stationDepartures, int limit) {
        if(stationDepartures == null)
            return null;
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        Calendar cal = Calendar.getInstance();
        int count = 0;
        for (Departure departure : stationDepartures.departures) {
            long time = 0;
            if (departure.predictedTime != null && departure.predictedTime.after(departure.plannedTime)) {
                time = departure.predictedTime.getTime();
            } else {
                time = departure.plannedTime.getTime();
            }
            time = (time - cal.getTimeInMillis());
            float depMinutes = (float) time / 1000 / 60;
            sb.append("[\"" + departure.line.label + "\",\"" + departure.destination.name + "\",\"" + (int) Math.ceil(depMinutes) + "\"],");
            count++;
            if (count >= limit)
                break;
        }
        String lines = sb.toString();
        if(lines.lastIndexOf(',') == -1)
            return lines+"]";
        return lines.substring(0, lines.lastIndexOf(',')) + "]";
    }


    private List<DepartureData> convertDepartures(StationDepartures stationDepartures) {
        Calendar cal = Calendar.getInstance();
        List<DepartureData> list = new ArrayList();
        if(stationDepartures == null)
            return list;
        for (Departure departure : stationDepartures.departures) {
            DepartureData data = new DepartureData();
            data.setTo(departure.destination.name);
            data.setToId(departure.destination.id);
            if(departure.line.product != null)
                data.setProduct(departure.line.product.toString());
            else
                data.setProduct("Unknown");
            data.setNumber(departure.line.label);
            if (departure.position != null)
                data.setPlatform(departure.position.name);
            long time;
            //Predicted time
            if (departure.predictedTime != null && departure.predictedTime.after(departure.plannedTime)) {
                data.setDepartureTime(df.format(departure.predictedTime));
                data.setDepartureTimestamp(departure.predictedTime.getTime());
                data.setDepartureDelay((departure.predictedTime.getTime() - departure.plannedTime.getTime()) / 1000 / 60);
                time = departure.predictedTime.getTime();
            } else {
                data.setDepartureTime(df.format(departure.plannedTime));
                data.setDepartureTimestamp(departure.plannedTime.getTime());
                time = departure.plannedTime.getTime();
            }
            time = (time - cal.getTimeInMillis());
            float depMinutes = (float) time / 1000 / 60;
            data.setDepartureTimeInMinutes((int) Math.ceil(depMinutes));
            list.add(data);
        }
        return list;
    }

}
