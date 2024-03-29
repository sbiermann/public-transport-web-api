package com.ems.publictransport.rest;

import com.ems.publictransport.util.ProviderUtil;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NvbwProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.SuggestLocationsResult;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Timed
@RestController
@RequestMapping(value = "rest/station")
public class StationController {


    private ProviderUtil providerUtil;

    public StationController(ProviderUtil providerUtil) {
        this.providerUtil = providerUtil;
    }

    @GetMapping("suggest")
    public ResponseEntity suggest(@RequestParam("q") final String query, @RequestParam(value = "provider", defaultValue = "Nvbw") String providerName,
                            @RequestParam(value = "locationType", defaultValue = "ANY") String stationType) throws IOException {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new NvbwProvider();
        if (provider == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider " + providerName + " not found or can not instantiated...");
        try {
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
                return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(resultList);
            } else {
                return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Remote Service is down or temporarily not available");
            }
        } catch ( SocketTimeoutException | SocketException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Timeout, Provider " + providerName + " not responding in 15 seconds or closed the connection");
        }
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

}
