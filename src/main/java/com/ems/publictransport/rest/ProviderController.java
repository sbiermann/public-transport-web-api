package com.ems.publictransport.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ems.publictransport.rest.resource.Provider;

import de.schildbach.pte.NetworkId;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;


@Timed
@RestController
@RequestMapping(value = "rest/provider")
public class ProviderController {

    private static Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @PostConstruct
    public void postConstruct()
    {
         logger.info("post construct called");
    }


    @RequestMapping
    public ResponseEntity providerlist() throws IOException {
        List<Provider> list = Arrays.stream( NetworkId.values() ).map( networkId -> {
            Provider provider = new Provider();
            provider.setName( CaseUtils.toCamelCase( networkId.name(), true, ' ' ) );
            return provider;
        } ).collect( Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(list);
    }

}
