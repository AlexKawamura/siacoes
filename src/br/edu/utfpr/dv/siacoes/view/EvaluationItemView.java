package br.edu.utfpr.dv.siacoes.view;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;

import br.edu.utfpr.dv.siacoes.Session;
import br.edu.utfpr.dv.siacoes.bo.EvaluationItemBO;
import br.edu.utfpr.dv.siacoes.components.StageComboBox;
import br.edu.utfpr.dv.siacoes.model.EvaluationItem;
import br.edu.utfpr.dv.siacoes.model.Module.SystemModule;
import br.edu.utfpr.dv.siacoes.model.User.UserProfile;
import br.edu.utfpr.dv.siacoes.window.EditEvaluationItemWindow;

public class EvaluationItemView extends ListView {
	
	public static final String NAME = "evaluationitem";
	
	private final StageComboBox comboStage;
	private final CheckBox checkActive;
	private final Button buttonMoveUp;
	private final Button buttonMoveDown;
	
	public EvaluationItemView(){
		super(SystemModule.SIGET);
		
		this.setCaption("Quesitos de Avalia��o");
		
		this.setProfilePerimissions(UserProfile.MANAGER);
		
		this.comboStage = new StageComboBox(true);
		this.comboStage.selectBoth();
		
		this.checkActive = new CheckBox("Listar apenas quesitos ativos");
		this.checkActive.setValue(true);
		
		HorizontalLayout h = new HorizontalLayout(this.comboStage, this.checkActive);
		h.setComponentAlignment(this.checkActive, Alignment.MIDDLE_LEFT);
		
		this.addFilterField(h);
		
		this.buttonMoveUp = new Button("Para Cima", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            	moveUp();
            }
        });
		this.buttonMoveUp.setEnabled(false);
    	
    	this.buttonMoveDown = new Button("Para Baixo", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            	moveDown();
            }
        });
    	this.buttonMoveDown.setEnabled(false);
    	
    	this.addActionButton(this.buttonMoveUp);
    	this.addActionButton(this.buttonMoveDown);
	}

	@Override
	protected void loadGrid() {
		this.getGrid().addColumn("TCC", Integer.class);
		this.getGrid().addColumn("Avalia��o", String.class);
		this.getGrid().addColumn("Quesito", String.class);
		this.getGrid().addColumn("Peso", Double.class);
		
		try {
			int stage = 0;
			
			if((this.comboStage != null) && (this.comboStage.getValue() != null) && !this.comboStage.getValue().toString().toLowerCase().equals("ambos")){
				stage = (int)this.comboStage.getValue();
			}
			
			EvaluationItemBO bo = new EvaluationItemBO();
	    	List<EvaluationItem> list = bo.listByStage(stage, Session.getUser().getDepartment().getIdDepartment(), this.checkActive.getValue());
	    	
	    	for(EvaluationItem item : list){
				Object itemId = this.getGrid().addRow(item.getStage(), item.getType().toString(), item.getDescription(), item.getPonderosity());
				this.addRowId(itemId, item.getIdEvaluationItem());
			}
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Listar Quesitos", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	@Override
	public void addClick() {
		EvaluationItem item = new EvaluationItem();
		
		item.setDepartment(Session.getUser().getDepartment());
		
		if(!this.comboStage.getValue().toString().toLowerCase().equals("ambos")){
			item.setStage((int)this.comboStage.getValue());
		}
		
		UI.getCurrent().addWindow(new EditEvaluationItemWindow(item, this));
	}

	@Override
	public void editClick(Object id) {
		try {
			EvaluationItemBO bo = new EvaluationItemBO();
			EvaluationItem item = bo.findById((int)id);
			
			UI.getCurrent().addWindow(new EditEvaluationItemWindow(item, this));
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Editar Quesito", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	@Override
	public void deleteClick(Object id) {
		try {
			EvaluationItemBO bo = new EvaluationItemBO();
			
			bo.delete((int)id);
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Excluir Quesito", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
	
	private void moveUp(){
		Object value = getIdSelected();
    	
    	if(value != null){
    		try{
    			EvaluationItemBO bo = new EvaluationItemBO();
    			
    			bo.moveUp((int)value);
    			this.refreshGrid();
    		}catch(Exception e){
    			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
    			
    			Notification.show("Mover Quesito", e.getMessage(), Notification.Type.ERROR_MESSAGE);
    		}
    	}else{
    		Notification.show("Mover Quesito", "Selecione o registro.", Notification.Type.WARNING_MESSAGE);
    	}
	}
	
	private void moveDown(){
		Object value = getIdSelected();
    	
    	if(value != null){
    		try{
    			EvaluationItemBO bo = new EvaluationItemBO();
    			
    			bo.moveDown((int)value);
    			this.refreshGrid();
    		}catch(Exception e){
    			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
    			
    			Notification.show("Mover Quesito", e.getMessage(), Notification.Type.ERROR_MESSAGE);
    		}
    	}else{
    		Notification.show("Mover Quesito", "Selecione o registro.", Notification.Type.WARNING_MESSAGE);
    	}
	}

	@Override
	public void filterClick() throws Exception {
		this.buttonMoveUp.setEnabled(!this.comboStage.getValue().toString().toLowerCase().equals("ambos"));
		this.buttonMoveDown.setEnabled(!this.comboStage.getValue().toString().toLowerCase().equals("ambos"));
	}

}
