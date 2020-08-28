package br.edu.utfpr.dv.siacoes.model;

import java.io.Serializable;

import lombok.Data;

public @Data class Campus implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int idCampus;
	private String name;
	private String address;
	private transient byte[] logo;
	private boolean active;
	private String site;
	private String initials;

}
