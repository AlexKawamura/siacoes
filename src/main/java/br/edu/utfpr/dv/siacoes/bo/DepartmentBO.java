package br.edu.utfpr.dv.siacoes.bo;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.edu.utfpr.dv.siacoes.model.Department;

public abstract class DepartmentBO {
	public abstract Department findByIdDAO(int id) throws SQLException;
	public abstract List<Department> listAllDAO(boolean onlyActive) throws SQLException;
	public abstract List<Department> listByCampusDAO(int idCampus, boolean onlyActive) throws SQLException;
	public abstract int saveDAO(int idUser, Department department) throws SQLException;
	
	public Department findById(int id) throws Exception{
		try{
			return findByIdDAO(id);
		}catch(Exception e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e.getMessage());
		}
	}
	
	public List<Department> listAll(boolean onlyActive) throws Exception{
		try{
			return listAllDAO(onlyActive);
		}catch(Exception e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e.getMessage());
		}
	}
	
	public List<Department> listByCampus(int idCampus, boolean onlyActive) throws Exception{
		try{
			return listByCampusDAO(idCampus, onlyActive);
		}catch(Exception e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e.getMessage());
		}
	}
	
	public int save(int idUser, Department department) throws Exception{
		if((department.getCampus() == null) || (department.getCampus().getIdCampus() == 0)){
			throw new Exception("Informe o câmpus do departamento.");
		}
		if(department.getName().isEmpty()){
			throw new Exception("Informe o nome do departamento.");
		}
		
		try{
			return saveDAO(idUser, department);
		}catch(Exception e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e.getMessage());
		}
	}

}
