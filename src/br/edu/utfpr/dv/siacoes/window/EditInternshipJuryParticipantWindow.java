package br.edu.utfpr.dv.siacoes.window;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.ui.Notification;

import br.edu.utfpr.dv.siacoes.components.StudentComboBox;

public class EditInternshipJuryParticipantWindow extends EditWindow {
	
	private final EditInternshipJuryWindow parentWindow;
	
	private final StudentComboBox comboStudent;
	
	public EditInternshipJuryParticipantWindow(EditInternshipJuryWindow parentWindow){
		super("Adicionar Acad�mico", null);
		
		this.parentWindow = parentWindow;
		
		this.comboStudent = new StudentComboBox("Acad�mico");
		
		this.addField(this.comboStudent);
	}
	
	@Override
	public void save() {
		try{
			if((this.comboStudent.getStudent() == null) || (this.comboStudent.getStudent().getIdUser() == 0)){
				throw new Exception("Selecione o acad�mico.");
			}
			
			this.parentWindow.addParticipant(this.comboStudent.getStudent());
			
			this.close();
		}catch(Exception e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Adicionar Acad�mico", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

}
