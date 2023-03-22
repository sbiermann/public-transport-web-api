package com.ems.publictransport.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ems.publictransport.rest.resource.Provider;

import de.schildbach.pte.NetworkProvider;


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
        List<Provider> list = new ArrayList();
        Set<Class<? extends NetworkProvider>> reflection = new Reflections("de.schildbach.pte").getSubTypesOf(NetworkProvider.class);
        for (Class<? extends NetworkProvider> implClass : reflection) {
            if(implClass.getSimpleName().startsWith("Abstract"))
                continue;
            Provider provider = new Provider();
            provider.setName(implClass.getSimpleName().substring(0, implClass.getSimpleName().indexOf("Provider")));
            provider.setClass(implClass.getSimpleName());
            list.add(provider);
        }
        return ResponseEntity.status(HttpStatus.OK).headers(h -> h.set( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).body(list);
    }

}
