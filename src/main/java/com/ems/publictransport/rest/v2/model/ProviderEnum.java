/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ems.publictransport.rest.v2.model;

import java.lang.reflect.InvocationTargetException;

import com.ems.publictransport.rest.v2.PropertyReader;

import de.schildbach.pte.BvgProvider;
import de.schildbach.pte.KvvProvider;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.VbbProvider;

/**
 *
 * @author constantin
 */
public enum ProviderEnum {

    KVV(KvvProvider.class), BVG(BvgProvider.class), VBB(VbbProvider.class);

    private final Class<? extends NetworkProvider> providerClass;

    ProviderEnum(Class<? extends NetworkProvider> providerClass) {
        this.providerClass = providerClass;
    }

    public String label() {
        return PropertyReader.INSTANCE.getProperty("com/ems/publictransport/rest/v2/provider.properties", this.name().toLowerCase(), this.name());
    }

    public NetworkProvider newNetworkProvider() {
        try {
            if(providerClass.getName().equals(BvgProvider.class.getName()))
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance("{\"aid\":\"1Rxs112shyHLatUX4fofnmdxK\",\"type\":\"AID\"}");
            return providerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("error on instantiation of networkprovider '" + name() + "'",ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("error on instantiation of networkprovider '" + name() + "'",ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("error on instantiation of networkprovider '" + name() + "'",ex);
        }
    }
    
    public Provider asProvider() {
        Provider provider = new Provider();
        provider.setDescription(this.label());
        provider.setName(this.name());                
        return provider;
    }

}
