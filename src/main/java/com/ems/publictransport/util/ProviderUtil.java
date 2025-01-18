package com.ems.publictransport.util;

import de.schildbach.pte.*;
import okhttp3.HttpUrl;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


@Service
@EnableConfigurationProperties( ProviderKeysConfiguration.class )
public class ProviderUtil
{

	private static final Logger logger = LoggerFactory.getLogger( ProviderUtil.class );

	private final ProviderKeysConfiguration providerKeysConfiguration;

	private static final byte[] VRS_CLIENT_CERTIFICATE = Base64.decodeBase64(
			"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" );

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.54";


	public ProviderUtil( ProviderKeysConfiguration providerKeysConfiguration )
	{
		this.providerKeysConfiguration = providerKeysConfiguration;
	}


	public NetworkProvider getObjectForProvider( String providerName )
	{
		if( providerName == null || providerName.length() < 1 )
			return null;
		NetworkId networkId = NetworkId.valueOf( providerName.toUpperCase() );
		logger.debug( "found NetworkId: " + networkId );
		try
		{

			AbstractNetworkProvider provider = foundForId( networkId );
			provider.setUserAgent( USER_AGENT );
			return provider;
		}
		catch( UnsupportedEncodingException e )
		{
			logger.error( "could not parse url for provider: " + networkId );
		}
		catch( IllegalArgumentException e )
		{
			logger.error( "unknown provider used: " + networkId );
		}
		return null;
	}


	private AbstractNetworkProvider foundForId( NetworkId networkId ) throws UnsupportedEncodingException
	{
		if (networkId.equals(NetworkId.RT))
			return new RtProvider();
		else if (networkId.equals(NetworkId.DB))
			return new DbProvider();
		else if (networkId.equals(NetworkId.BVG))
			return new BvgProvider("{\"aid\":\"1Rxs112shyHLatUX4fofnmdxK\",\"type\":\"AID\"}");
		else if (networkId.equals(NetworkId.VBB))
			return new VbbProvider("{\"type\":\"AID\",\"aid\":\"hafas-vbb-apps\"}",
					"RCTJM2fFxFfxxQfI".getBytes(StandardCharsets.UTF_8));
		else if (networkId.equals(NetworkId.NVV))
			return new NvvProvider("{\"type\":\"AID\",\"aid\":\"Kt8eNOH7qjVeSxNA\"}");
		else if (networkId.equals(NetworkId.BAYERN))
			return new BayernProvider();
		else if (networkId.equals(NetworkId.MVV))
			return new MvvProvider();
		else if (networkId.equals(NetworkId.INVG))
			return new InvgProvider("{\"type\":\"AID\",\"aid\":\"GITvwi3BGOmTQ2a5\"}",
					"ERxotxpwFT7uYRsI".getBytes(StandardCharsets.UTF_8));
		else if (networkId.equals(NetworkId.AVV_AUGSBURG))
			return new AvvAugsburgProvider("{\"type\":\"AID\",\"aid\":\"jK91AVVZU77xY5oH\"}");
		else if (networkId.equals(NetworkId.VGN))
			return new VgnProvider(HttpUrl.parse("https://efa.vgn.de/vgnExt_oeffi/"));
		else if (networkId.equals(NetworkId.VVM))
			return new VvmProvider();
		else if (networkId.equals(NetworkId.VMV))
			return new VmvProvider();
		else if (networkId.equals(NetworkId.SH))
			return new ShProvider("{\"aid\":\"r0Ot9FLFNAFxijLW\",\"type\":\"AID\"}");
		else if (networkId.equals(NetworkId.GVH))
			return new GvhProvider();
		else if (networkId.equals(NetworkId.BSVAG))
			return new BsvagProvider();
		else if (networkId.equals(NetworkId.VBN))
			return new VbnProvider("{\"aid\":\"rnOHBWhesvc7gFkd\",\"type\":\"AID\"}",
					"SP31mBufSyCLmNxp".getBytes(StandardCharsets.UTF_8));
		else if (networkId.equals(NetworkId.NASA))
			return new NasaProvider("{\"type\":\"AID\",\"aid\":\"nasa-apps\"}");
		else if (networkId.equals(NetworkId.VMT))
			return new VmtProvider("{\"aid\":\"vj5d7i3g9m5d7e3\",\"type\":\"AID\"}");
		else if (networkId.equals(NetworkId.VVO))
			return new VvoProvider(HttpUrl.parse("https://efa.vvo-online.de/Oeffi/"));
		else if (networkId.equals(NetworkId.VRR))
			return new VrrProvider();
		else if (networkId.equals(NetworkId.VRS))
			return new VrsProvider(VRS_CLIENT_CERTIFICATE);
		else if (networkId.equals(NetworkId.AVV_AACHEN))
			return new AvvAachenProvider("{\"id\":\"AVV_AACHEN\",\"l\":\"vs_oeffi\",\"type\":\"WEB\"}",
					"{\"type\":\"AID\",\"aid\":\"4vV1AcH3N511icH\"}");
		else if (networkId.equals(NetworkId.MVG))
			return new MvgProvider();
		else if (networkId.equals(NetworkId.VRN))
			return new VrnProvider();
		else if (networkId.equals(NetworkId.VVS))
			return new VvsProvider(HttpUrl.parse("https://www2.vvs.de/oeffi/"));
		else if (networkId.equals(NetworkId.DING))
			return new DingProvider();
		else if (networkId.equals(NetworkId.KVV))
			return new KvvProvider(HttpUrl.parse("https://projekte.kvv-efa.de/oeffi/"));
		else if (networkId.equals(NetworkId.NVBW))
			return new NvbwProvider();
		else if (networkId.equals(NetworkId.VVV))
			return new VvvProvider();
		else if (networkId.equals(NetworkId.OEBB))
			return new OebbProvider("{\"type\":\"AID\",\"aid\":\"OWDL4fE4ixNiPBBm\"}");
		else if (networkId.equals(NetworkId.WIEN))
			return new WienProvider();
		else if (networkId.equals(NetworkId.LINZ))
			return new LinzProvider();
		else if (networkId.equals(NetworkId.STV))
			return new StvProvider();
		else if (networkId.equals(NetworkId.VBL))
			return new VblProvider();
		else if (networkId.equals(NetworkId.ZVV))
			return new ZvvProvider("{\"type\":\"AID\",\"aid\":\"hf7mcf9bv3nv8g5f\"}");
		else if (networkId.equals(NetworkId.LU))
			return new LuProvider("{\"type\":\"AID\",\"aid\":\"SkC81GuwuzL4e0\"}");
		else if (networkId.equals(NetworkId.NS))
			return new NsProvider();
		else if (networkId.equals(NetworkId.DSB))
			return new DsbProvider("{\"type\":\"AID\",\"aid\":\"irkmpm9mdznstenr-android\"}");
		else if (networkId.equals(NetworkId.SE))
			return new SeProvider("{\"type\":\"AID\",\"aid\":\"h5o3n7f4t2m8l9x1\"}");
		else if (networkId.equals(NetworkId.TLEM))
			return new TlemProvider();
		else if (networkId.equals(NetworkId.MERSEY))
			return new MerseyProvider();
		else if (networkId.equals(NetworkId.DUB))
			return new DubProvider();
		else if (networkId.equals(NetworkId.BART))
			return new BartProvider("{\"type\":\"AID\",\"aid\":\"kEwHkFUCIL500dym\"}");
		else if (networkId.equals(NetworkId.RTACHICAGO))
			return new RtaChicagoProvider();
		else if (networkId.equals(NetworkId.CMTA))
			return new CmtaProvider("{\"type\":\"AID\",\"aid\":\"web9j2nak29uz41irb\"}");
		else if (networkId.equals(NetworkId.SYDNEY))
			return new SydneyProvider();
		else
			throw new IllegalArgumentException(networkId.name());

	}

}
