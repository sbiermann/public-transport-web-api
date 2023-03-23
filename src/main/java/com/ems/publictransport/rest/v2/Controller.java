/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ems.publictransport.rest.v2;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ems.publictransport.rest.v2.model.DepartureData;
import com.ems.publictransport.rest.v2.model.Provider;
import com.ems.publictransport.rest.v2.model.ProviderEnum;

import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;
import de.schildbach.pte.dto.SuggestLocationsResult;
import io.micrometer.core.annotation.Timed;

/**
 * @author constantin
 */
@Timed
@RestController
@RequestMapping(value = "v2")
public class Controller {


    @RequestMapping(value = "provider", method = RequestMethod.GET)
    public ResponseEntity providerlist() throws IOException {
        List<Provider> list = new ArrayList();
        for (ProviderEnum each : ProviderEnum.values()) {
            list.add(each.asProvider());
        }
        return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(list);
    }

    @RequestMapping(value = "station/nearby", method = RequestMethod.GET)
    public ResponseEntity findNearbyLocations(@RequestParam("provider") String providerName) {
        NetworkProvider networkProvider = getNetworkProvider(providerName);
        return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(networkProvider != null ? networkProvider.defaultProducts() : "");
    }


    @RequestMapping(value = "station/suggest", method = RequestMethod.GET)
    public ResponseEntity suggest( @RequestParam("q") final String query, @RequestParam("provider") String providerName,
                            @RequestParam("locationType") String stationType) throws IOException {
        NetworkProvider provider = getNetworkProvider(providerName);
        if (provider == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider " + providerName + " not found or can not instantiated...");

        SuggestLocationsResult suggestLocations = provider.suggestLocations(query, null, 10);
        if (SuggestLocationsResult.Status.OK.equals(suggestLocations.status)) {
            Iterator<Location> iterator = suggestLocations.getLocations().iterator();
            LocationType locationType = getLocationType(stationType);
            List<Location> resultList = new ArrayList<>();
            if (locationType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("LocationType " + stationType + " not found or can not instantiated...");
            } else if (!LocationType.ANY.equals(locationType)) {
                while (iterator.hasNext()) {
                    Location loc = iterator.next();
                    if (locationType.equals(loc.type)) {
                        resultList.add(loc);
                    }
                }
            } else {
                resultList.addAll(suggestLocations.getLocations());
            }
            return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(resultList);
        } else {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Remote Service is down or temporarily not available");
        }
    }

    @RequestMapping(value = "departure", method = RequestMethod.GET)
    public ResponseEntity departure( @RequestParam("from") String from, @RequestParam("provider") String providerName,
                              @RequestParam(value = "limit", defaultValue = "10")  int limit, @RequestParam("numberFilter") String numberFilter,
                              @RequestParam("toFilter") String toFilter) throws IOException {
        NetworkProvider provider = getNetworkProvider(providerName);
        if (provider == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider " + providerName + " not found or can not instantiated...");
        }
        QueryDeparturesResult efaData = provider.queryDepartures(from, new Date(), 120, true);
        if (efaData.status.name().equals("OK")) {
            List<DepartureData> list = new ArrayList<>();
            if (efaData.findStationDepartures(from) == null && !efaData.stationDepartures.isEmpty()) {
                for (StationDepartures stationDeparture : efaData.stationDepartures) {
                    list.addAll(convertDepartures(stationDeparture, numberFilter, toFilter));
                }
                Collections.sort(list);
            } else {
                list.addAll(convertDepartures(efaData.findStationDepartures(from), numberFilter, toFilter));
            }
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(list);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "EFA error status: " + efaData.status.name());

    }

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");


    private List<DepartureData> convertDepartures(StationDepartures stationDepartures, String numberFilter, String toFilter) {
        Calendar cal = Calendar.getInstance();
        List<DepartureData> list = new ArrayList();
        for (Departure departure : stationDepartures.departures) {
            if (!isIncluded(departure, numberFilter, toFilter)) {
                continue;
            }
            DepartureData data = new DepartureData();
            data.setMessage(departure.message);
            data.setTo(departure.destination.name);
            data.setToId(departure.destination.id);
            data.setProduct(departure.line.product.toString());
            data.setNumber(departure.line.label);
            if (departure.position != null) {
                data.setPlatform(departure.position.name);
            }
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

    private LocationType getLocationType(String locationType) {
        if (locationType == null || "*".equals(locationType)) {
            return LocationType.ANY;
        } else {
            try {
                return LocationType.valueOf(locationType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private boolean isIncluded(Departure stationDeparture, String numberFilter, String toFilter) {
        if (toFilter != null && !toFilter.equals("*")) {
            Location dest = stationDeparture.destination;
            if (!(dest != null && dest.name != null && toFilter.equalsIgnoreCase(dest.name))) {
                return false;
            }
        }
        if (numberFilter != null && !numberFilter.equals("*")) {
            Line line = stationDeparture.line;
            if (!(line != null && line.label != null && numberFilter.equalsIgnoreCase(line.label))) {
                return false;
            }
        }
        return true;
    }

    private NetworkProvider getNetworkProvider(String providerName) {
        try {
            if (providerName != null) {
                ProviderEnum provider = ProviderEnum.valueOf(providerName.toUpperCase());
                return provider.newNetworkProvider();
            }
        } catch (RuntimeException e) {
        }
        return null;
    }

}
