package siacoes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.utfpr.dv.siacoes.model.Department;

class TesteModelsDepartmentAndCampus {
	private final Department department = new Department();

    @Test
    @DisplayName("Testando método getName da classe Department")
    void testeDepartment() {
    	String name = "Teste Departamento";
    	department.setName(name);
    	
    	assertEquals(name, department.getName());
    }
    
    @Test
    @DisplayName("Testando método getName da classe Campus")
    void testeCampus() {
    	String name = "Teste Campus";
    	
    	department.setName(name);
    	
    	assertEquals(name, department.getName());
    }
}
