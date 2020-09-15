package br.edu.utfpr.dv.siacoes.dao;

public abstract class TemplateMethod<T>{
	public abstract T findById(int id) throws Exception;
}
