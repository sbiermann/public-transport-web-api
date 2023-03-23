package com.ems.publictransport.util;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties( "provider.keys" )
public class ProviderKeysConfiguration
{
	private String navitia;

	private String bvg;

	private String oebb;

	private String nvv;

	private String invg;

	private String vgn;

	private String sh;

	private String gvh;

	private String vbn;

	private String vmt;

	private String avvaachen;

	private String vvs;

	private String kvv;

	private String zvv;

	private String lu;

	private String dsb;

	private String se;

	private String bahn;

	private String nasa;

	private String vbb;

	private String vvo;

	private String sncb;

	private String bart;

	private String cmta;


	public String getCmta()
	{
		return cmta;
	}


	public void setCmta( String cmta )
	{
		this.cmta = cmta;
	}


	public String getBart()
	{
		return bart;
	}


	public void setBart( String bart )
	{
		this.bart = bart;
	}


	public String getSncb()
	{
		return sncb;
	}


	public void setSncb( String sncb )
	{
		this.sncb = sncb;
	}


	public String getNavitia()
	{
		return navitia;
	}


	public void setNavitia( String navitia )
	{
		this.navitia = navitia;
	}


	public String getBvg()
	{
		return bvg;
	}


	public void setBvg( String bvg )
	{
		this.bvg = bvg;
	}


	public String getOebb()
	{
		return oebb;
	}


	public void setOebb( String oebb )
	{
		this.oebb = oebb;
	}


	public String getNvv()
	{
		return nvv;
	}


	public void setNvv( String nvv )
	{
		this.nvv = nvv;
	}


	public String getInvg()
	{
		return invg;
	}


	public void setInvg( String invg )
	{
		this.invg = invg;
	}


	public String getVgn()
	{
		return vgn;
	}


	public void setVgn( String vgn )
	{
		this.vgn = vgn;
	}


	public String getSh()
	{
		return sh;
	}


	public void setSh( String sh )
	{
		this.sh = sh;
	}


	public String getGvh()
	{
		return gvh;
	}


	public void setGvh( String gvh )
	{
		this.gvh = gvh;
	}


	public String getVbn()
	{
		return vbn;
	}


	public void setVbn( String vbn )
	{
		this.vbn = vbn;
	}


	public String getVmt()
	{
		return vmt;
	}


	public void setVmt( String vmt )
	{
		this.vmt = vmt;
	}


	public String getAvvaachen()
	{
		return avvaachen;
	}


	public void setAvvaachen( String avvaachen )
	{
		this.avvaachen = avvaachen;
	}


	public String getVvs()
	{
		return vvs;
	}


	public void setVvs( String vvs )
	{
		this.vvs = vvs;
	}


	public String getKvv()
	{
		return kvv;
	}


	public void setKvv( String kvv )
	{
		this.kvv = kvv;
	}


	public String getZvv()
	{
		return zvv;
	}


	public void setZvv( String zvv )
	{
		this.zvv = zvv;
	}


	public String getLu()
	{
		return lu;
	}


	public void setLu( String lu )
	{
		this.lu = lu;
	}


	public String getDsb()
	{
		return dsb;
	}


	public void setDsb( String dsb )
	{
		this.dsb = dsb;
	}


	public String getSe()
	{
		return se;
	}


	public void setSe( String se )
	{
		this.se = se;
	}


	public String getBahn()
	{
		return bahn;
	}


	public void setBahn( String bahn )
	{
		this.bahn = bahn;
	}


	public String getNasa()
	{
		return nasa;
	}


	public void setNasa( String nasa )
	{
		this.nasa = nasa;
	}


	public String getVbb()
	{
		return vbb;
	}


	public void setVbb( String vbb )
	{
		this.vbb = vbb;
	}


	public String getVvo()
	{
		return vvo;
	}


	public void setVvo( String vvo )
	{
		this.vvo = vvo;
	}
}
