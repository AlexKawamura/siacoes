package br.edu.utfpr.dv.siacoes.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Campus implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int idCampus;
	private String name;
	private String address;
	private transient byte[] logo;
	private boolean active;
	private String site;
	private String initials;
	
	public Campus(){
		this.setIdCampus(0);
		this.setName("");
		this.setAddress("");
		this.setLogo(null);
		this.setActive(true);
		this.setSite("");
		this.setInitials("");
	}

}
