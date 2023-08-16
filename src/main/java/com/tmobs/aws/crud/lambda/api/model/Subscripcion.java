package com.tmobs.aws.crud.lambda.api.model;

import com.google.gson.Gson;

public class Subscripcion {
	
	private String id;
	private String msisdn;
	private String fecAlta;
	private String fecUpdated;
	private String fecBaja;
	private String producto;
	private int cantUpdated;
	
	public Subscripcion() {
		
	}
	
	public Subscripcion(String json) {
		super();
		
		Gson gson = new Gson();
		Subscripcion temp = gson.fromJson(json, Subscripcion.class);
		
		this.id      = temp.id;
		this.msisdn  = temp.msisdn;
		this.fecAlta = temp.fecAlta;
		this.fecBaja = temp.fecBaja;
		this.producto= temp.producto;
		this.cantUpdated = temp.cantUpdated;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public boolean isEnabled() {
		return fecBaja != null;
	}
	public String getProducto() {
		return producto;
	}
	public void setProducto(String producto) {
		this.producto = producto;
	}
	public int getCantUpdated() {
		return cantUpdated;
	}
	public void setCantUpdated(int updated) {
		this.cantUpdated = updated;
	}
	public String toString() {
		return new Gson().toJson(this);
	}
	
	public String getFecAlta() {
		return fecAlta;
	}
	public void setFecAlta(String fecAlta) {
		this.fecAlta = fecAlta;
	}
	public String getFecBaja() {
		return fecBaja;
	}
	public void setFecBaja(String fecBaja) {
		this.fecBaja = fecBaja;
	}
	public String getFecUpdated() {
		return fecUpdated;
	}
	public void setFecUpdated(String fecUpdated) {
		this.fecUpdated = fecUpdated;
	}
}
