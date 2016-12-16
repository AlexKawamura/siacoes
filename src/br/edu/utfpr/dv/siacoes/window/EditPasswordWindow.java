package br.edu.utfpr.dv.siacoes.window;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;

import br.edu.utfpr.dv.siacoes.Session;
import br.edu.utfpr.dv.siacoes.bo.UserBO;
import br.edu.utfpr.dv.siacoes.model.User;

public class EditPasswordWindow extends EditWindow {

	private final PasswordField textCurrentPassword;
	private final PasswordField textNewPassword;
	private final PasswordField textNewPassword2;
	
	public EditPasswordWindow(){
		super("Alterar Senha", null);
		
		this.textCurrentPassword = new PasswordField("Senha Atual");
		this.textCurrentPassword.setWidth("400px");
		
		this.textNewPassword = new PasswordField("Nova Senha");
		this.textNewPassword.setWidth("400px");
		
		this.textNewPassword2 = new PasswordField("Confirma��o de Senha");
		this.textNewPassword2.setWidth("400px");
		
		this.addField(this.textCurrentPassword);
		this.addField(this.textNewPassword);
		this.addField(this.textNewPassword2);
		
		this.textCurrentPassword.focus();
	}
	
	@Override
	public void save() {
		try {
			if(!this.textNewPassword.getValue().equals(this.textNewPassword2.getValue())){
				this.textNewPassword.focus();
				throw new Exception("As senhas n�o conferem.");
			}
			
			UserBO bo = new UserBO();
			User user = bo.changePassword(Session.getUser().getIdUser(), this.textCurrentPassword.getValue(), this.textNewPassword.getValue());
			
			Session.setUser(user);
			
			Notification.show("Alterar Senha", "Senha alterada com sucesso.", Notification.Type.HUMANIZED_MESSAGE);
			
			this.parentViewRefreshGrid();
			this.close();
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Alterar Senha", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

}
