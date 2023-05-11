package com.tmobs.aws.crud.lambda.api.model;

import com.google.gson.Gson;

public class Generic {

	private int id;
	private String entity;
	private String name;
	private String description;

	private String customStr1;
	private String customStr2;

	private int customInt1;
	private int customInt2;

	private double customDouble1;
	private double customDouble2;	

	public String toString() {
		return new Gson().toJson(this);
	}

	public Generic(String json) {
		super();

		Gson gson = new Gson();
		Generic temp = gson.fromJson(json, Generic.class);

		this.id = temp.id;
		this.entity      = temp.entity;
		this.name        = temp.name;
		this.description = temp.description;

		this.customStr1 = temp.customStr1;
		this.customStr2 = temp.customStr2;

		this.customInt1 = temp.customInt1;
		this.customInt2 = temp.customInt2;

		this.customDouble1 = temp.customDouble1;
		this.customDouble2 = temp.customDouble2;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCustomStr1() {
		return customStr1;
	}

	public void setCustomStr1(String customStr1) {
		this.customStr1 = customStr1;
	}

	public String getCustomStr2() {
		return customStr2;
	}

	public void setCustomStr2(String customStr2) {
		this.customStr2 = customStr2;
	}

	public int getCustomInt1() {
		return customInt1;
	}

	public void setCustomInt1(int customInt1) {
		this.customInt1 = customInt1;
	}

	public int getCustomInt2() {
		return customInt2;
	}

	public void setCustomInt2(int customInt2) {
		this.customInt2 = customInt2;
	}

	public double getCustomDouble1() {
		return customDouble1;
	}

	public void setCustomDouble1(double customDouble1) {
		this.customDouble1 = customDouble1;
	}

	public double getCustomDouble2() {
		return customDouble2;
	}

	public void setCustomDouble2(double customDouble2) {
		this.customDouble2 = customDouble2;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}
}