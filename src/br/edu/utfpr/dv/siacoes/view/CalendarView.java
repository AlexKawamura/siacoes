package br.edu.utfpr.dv.siacoes.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.renderers.DateRenderer;

import br.edu.utfpr.dv.siacoes.Session;
import br.edu.utfpr.dv.siacoes.bo.CertificateBO;
import br.edu.utfpr.dv.siacoes.bo.JuryAppraiserBO;
import br.edu.utfpr.dv.siacoes.bo.JuryBO;
import br.edu.utfpr.dv.siacoes.bo.JuryStudentBO;
import br.edu.utfpr.dv.siacoes.bo.ProjectBO;
import br.edu.utfpr.dv.siacoes.bo.ThesisBO;
import br.edu.utfpr.dv.siacoes.components.SemesterComboBox;
import br.edu.utfpr.dv.siacoes.components.YearField;
import br.edu.utfpr.dv.siacoes.model.CalendarReport;
import br.edu.utfpr.dv.siacoes.model.Jury;
import br.edu.utfpr.dv.siacoes.model.JuryAppraiser;
import br.edu.utfpr.dv.siacoes.model.JuryFormReport;
import br.edu.utfpr.dv.siacoes.model.JuryStudent;
import br.edu.utfpr.dv.siacoes.model.Project;
import br.edu.utfpr.dv.siacoes.model.TermOfApprovalReport;
import br.edu.utfpr.dv.siacoes.model.Thesis;
import br.edu.utfpr.dv.siacoes.model.Module.SystemModule;
import br.edu.utfpr.dv.siacoes.util.DateUtils;
import br.edu.utfpr.dv.siacoes.util.ExtensionUtils;
import br.edu.utfpr.dv.siacoes.util.ReportUtils;
import br.edu.utfpr.dv.siacoes.window.EditJuryAppraiserFeedbackWindow;
import br.edu.utfpr.dv.siacoes.window.EditJuryWindow;

public class CalendarView extends ListView {
	
	public static final String NAME = "calendar";
	
	private final SemesterComboBox comboSemester;
	private final YearField textYear;
	private final Button buttonFile;
	private final Button buttonForm;
	private final Button buttonTerm;
	private final Button buttonCalendar;
	private final Button buttonStatements;
	private final Button buttonSingleStatement;
	private final Button buttonSendFeedback;
	
	private Button.ClickListener listenerClickFile;
	private Button.ClickListener listenerClickForm;
	private Button.ClickListener listenerClickTerm;
	
	private boolean listAll = false;

	public CalendarView(){
		super(SystemModule.SIGET);
		
		this.comboSemester = new SemesterComboBox();
		this.comboSemester.select(DateUtils.getSemester());
		
		this.textYear = new YearField();
		this.textYear.setValue(String.valueOf(DateUtils.getYear()));
		
		this.addFilterField(new HorizontalLayout(this.comboSemester, this.textYear));
		
		this.setAddVisible(false);
		this.setEditVisible(false);
		this.setDeleteVisible(false);
		
		this.buttonFile = new Button("Trabalho");
		
		this.buttonForm = new Button("Ficha");
		
		this.buttonTerm = new Button("Termo");
		
		this.buttonSendFeedback = new Button("Feedback", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            	sendFeedbackClick();
            }
        });
		
		this.buttonStatements = new Button("Declara��es", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            	downloadStatements();
            }
        });
		
		this.buttonSingleStatement = new Button("Declara��o", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            	downloadSingleStagement();
            }
        });
		
		this.buttonCalendar = new Button("Imprimir");
		
		this.addActionButton(this.buttonCalendar);
		this.addActionButton(this.buttonFile);
		this.addActionButton(this.buttonForm);
		this.addActionButton(this.buttonTerm);
		this.addActionButton(this.buttonSendFeedback);
		this.addActionButton(this.buttonSingleStatement);
		
		if(Session.isUserManager(this.getModule())){
			this.addActionButton(this.buttonStatements);
			this.setEditVisible(true);
			this.setEditCaption("Banca");
		}
	}
	
	private void configureButtons(){
		if(this.listAll){
			this.buttonSendFeedback.setVisible(false);
			this.buttonTerm.setVisible(Session.isUserManager(this.getModule()));
			this.buttonForm.setVisible(Session.isUserManager(this.getModule()));
			this.buttonSingleStatement.setVisible(false);
			this.buttonFile.setVisible(Session.isUserManager(this.getModule()));
		}else{
			this.buttonSendFeedback.setVisible(true);
			this.buttonTerm.setVisible(false);
			this.buttonFile.setVisible(Session.isUserProfessor());
			this.buttonForm.setVisible(Session.isUserProfessor());
			this.buttonSendFeedback.setVisible(Session.isUserProfessor());
			this.buttonStatements.setVisible(false);
			this.buttonCalendar.setVisible(Session.isUserProfessor());
		}
	}
	
	@Override
	protected void loadGrid() {
		this.getGrid().addColumn("Data e Hora", Date.class).setRenderer(new DateRenderer(new SimpleDateFormat("dd/MM/yyyy HH:mm")));
		this.getGrid().addColumn("Local", String.class);
		this.getGrid().addColumn("TCC", Integer.class);
		this.getGrid().addColumn("T�tulo", String.class);
		this.getGrid().addColumn("Acad�mico", String.class);
		this.getGrid().addSelectionListener(new SelectionListener() {
			@Override
			public void select(SelectionEvent event) {
				prepareDownload();
			}
		});
		
		this.getGrid().getColumns().get(0).setWidth(165);
		this.getGrid().getColumns().get(2).setWidth(65);
		
		this.prepareDownload();
		
		try {
			JuryBO bo = new JuryBO();
			List<Jury> list;
			List<CalendarReport> report = null;
			
			if(this.listAll){
				list = bo.listBySemester(Session.getUser().getDepartment().getIdDepartment(), this.comboSemester.getSemester(), this.textYear.getYear());
				report = bo.getCalendarReport(Session.getUser().getDepartment().getIdDepartment(), 0, this.comboSemester.getSemester(), this.textYear.getYear());
			}else{
				if(Session.isUserProfessor()){
					list = bo.listByAppraiser(Session.getUser().getIdUser(), this.comboSemester.getSemester(), this.textYear.getYear());
					report = bo.getCalendarReport(Session.getUser().getDepartment().getIdDepartment(), Session.getUser().getIdUser(), this.comboSemester.getSemester(), this.textYear.getYear());
				}else{
					list = bo.listByStudent(Session.getUser().getIdUser(), this.comboSemester.getSemester(), this.textYear.getYear());
				}
			}
			
			if(this.listAll || Session.isUserProfessor()){
				new ReportUtils().prepareForPdfReport("Calendar", "Agenda de Defesas", report, this.buttonCalendar);
			}
			
	    	for(Jury jury : list){
	    		String title = "";
	    		String student = "";
	    		
	    		if((jury.getThesis() != null) && (jury.getThesis().getIdThesis() != 0)){
	    			ThesisBO tbo = new ThesisBO();
	    			Thesis thesis = tbo.findById(jury.getThesis().getIdThesis());
	    			
	    			title = thesis.getTitle();
	    			student = thesis.getStudent().getName();
	    		}else{
	    			ProjectBO pbo = new ProjectBO();
	    			Project project = pbo.findById(jury.getProject().getIdProject());
	    			
	    			title = project.getTitle();
	    			student = project.getStudent().getName();
	    		}
	    		
				Object itemId = this.getGrid().addRow(jury.getDate(), jury.getLocal(), jury.getStage(), title, student);
				this.addRowId(itemId, jury.getIdJury());
			}
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Listar Bancas", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
	
	private void sendFeedbackClick(){
		Object id = getIdSelected();
    	
    	if(id == null){
    		Notification.show("Selecionar Registro", "Selecione o registro para enviar o feedback.", Notification.Type.WARNING_MESSAGE);
    	}else{
    		try {
    			JuryAppraiserBO bo = new JuryAppraiserBO();
    			
    			JuryAppraiser appraiser = bo.findByAppraiser((int)id, Session.getUser().getIdUser());
    			
    			if(appraiser != null){
    				UI.getCurrent().addWindow(new EditJuryAppraiserFeedbackWindow(appraiser));
    			}else{
    				Notification.show("Enviar Feedback", "� necess�rio ser membro da banca para enviar o feedback.", Notification.Type.WARNING_MESSAGE);
    			}
			} catch (Exception e) {
				Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
				
				Notification.show("Enviar Feedback", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			}
    	}
	}
	
	private void downloadSingleStagement(){
		Object value = getIdSelected();
		
		if(value == null){
			Notification.show("Gerar Declara��o", "Selecione uma banca para gerar a declara��o.", Notification.Type.WARNING_MESSAGE);
		}else{
			try{
				CertificateBO bo = new CertificateBO();
				byte[] report;
				
				if(Session.isUserProfessor()){
					JuryAppraiserBO jbo = new JuryAppraiserBO();
					JuryAppraiser appraiser = jbo.findByAppraiser((int)value, Session.getUser().getIdUser());
					
					report = bo.getJuryProfessorStatement(appraiser);
				}else{
					JuryStudentBO jbo = new JuryStudentBO();
					JuryStudent student = jbo.findByStudent((int)value, Session.getUser().getIdUser());
					
					report = bo.getJuryStudentStatement(student);
				}
				
				if(report != null){
					Session.putReport(report);
					
					getUI().getPage().open("#!" + CertificateView.NAME + "/session/" + UUID.randomUUID().toString(), "_blank");
				}else{
					Notification.show("Gerar Declara��o", "N�o foi encontrada a declara��o para imprimir.", Notification.Type.WARNING_MESSAGE);
				}
			}catch(Exception e){
				Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
	        	
	        	Notification.show("Gerar Declara��o", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			}
		}
	}
	
	private void downloadStatements(){
		Object value = getIdSelected();
		
		if(value == null){
			Notification.show("Gerar Declara��es", "Selecione uma banca para gerar as declara��es.", Notification.Type.WARNING_MESSAGE);
		}else{
			try{
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				PDFMergerUtility pdfMerge = new PDFMergerUtility();
				pdfMerge.setDestinationStream(output);
				
				CertificateBO bo = new CertificateBO();

				byte[] reportProfessor = bo.getJuryProfessorStatementReportList((int)value);
				
				if(reportProfessor != null){
					pdfMerge.addSource(new ByteArrayInputStream(reportProfessor));
				}
				
				byte[] reportStudent = bo.getJuryStudentStatementReportList((int)value);
				
				if(reportStudent != null){
					pdfMerge.addSource(new ByteArrayInputStream(reportStudent));
				}
				
				if((reportProfessor != null) || (reportStudent != null)){
					pdfMerge.mergeDocuments(null);
					
					byte[] report = output.toByteArray();
					
					Session.putReport(report);
					
					getUI().getPage().open("#!" + CertificateView.NAME + "/session/" + UUID.randomUUID().toString(), "_blank");
				}else{
					Notification.show("Gerar Declara��es", "N�o h� declara��es para serem geradas.", Notification.Type.WARNING_MESSAGE);
				}
			}catch(Exception e){
				Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
	        	
	        	Notification.show("Gerar Declara��es", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			}
		}
	}
	
	private void prepareDownload(){
		Object value = getIdSelected();
    	
		this.buttonForm.removeClickListener(this.listenerClickForm);
		this.buttonFile.removeClickListener(this.listenerClickFile);
		this.buttonTerm.removeClickListener(this.listenerClickTerm);
		
    	if(value != null){
    		if(this.buttonForm.isVisible()){
				try {
					JuryBO bo = new JuryBO();
					JuryFormReport report = bo.getFormReport((int)value);
					
					List<JuryFormReport> list = new ArrayList<JuryFormReport>();
					list.add(report);
					
					new ReportUtils().prepareForPdfReport("JuryForm", "Ficha de Avalia��o", list, this.buttonForm);
				} catch (Exception e) {
					this.listenerClickForm = new Button.ClickListener() {
			            @Override
			            public void buttonClick(ClickEvent event) {
			            	Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			            	
			            	Notification.show("Imprimir Ficha de Avalia��o", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			            }
			        };
			        
					this.buttonForm.addClickListener(this.listenerClickForm);
				}
    		}
			
    		if(this.buttonTerm.isVisible()){
				try {
					JuryBO bo = new JuryBO();
					TermOfApprovalReport report = bo.getTermOfApprovalReport((int)value);
					
					List<TermOfApprovalReport> list = new ArrayList<TermOfApprovalReport>();
					list.add(report);
					
					new ReportUtils().prepareForPdfReport("TermOfApproval", "Termo de Aprova��o", list, this.buttonTerm);
				} catch (Exception e) {
					this.listenerClickTerm = new Button.ClickListener() {
			            @Override
			            public void buttonClick(ClickEvent event) {
			            	Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			            	
			            	Notification.show("Imprimir Termo de Aprova��o", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			            }
			        };
			        
					this.buttonTerm.addClickListener(this.listenerClickTerm);
				}
    		}
			
    		if(this.buttonFile.isVisible()){
				try {
					JuryBO bo = new JuryBO();
					Jury jury = bo.findById((int)value);
					
					String fileName = "";
					byte[] file = null;
					
					if((jury.getThesis() != null) && (jury.getThesis().getIdThesis() != 0)){
		    			ThesisBO tbo = new ThesisBO();
		    			Thesis thesis = tbo.findById(jury.getThesis().getIdThesis());
		    			
		    			fileName = thesis.getTitle() + thesis.getFileType().getExtension();
		    			file = thesis.getFile();
		    		}else{
		    			ProjectBO pbo = new ProjectBO();
		    			Project project = pbo.findById(jury.getProject().getIdProject());
		    			
		    			fileName = project.getTitle() + project.getFileType().getExtension();
		    			file = project.getFile();
		    		}
					
					new ExtensionUtils().extendToDownload(fileName, file, this.buttonFile);
	        	} catch (Exception e) {
	        		this.listenerClickFile = new Button.ClickListener() {
			            @Override
			            public void buttonClick(ClickEvent event) {
			            	Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			            	
			            	Notification.show("Download do Trabalho", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			            }
			        };
			        
	        		this.buttonFile.addClickListener(this.listenerClickFile);
				}
    		}
    	}else{
    		new ExtensionUtils().removeAllExtensions(this.buttonFile);
    		new ExtensionUtils().removeAllExtensions(this.buttonForm);
    		new ExtensionUtils().removeAllExtensions(this.buttonTerm);
    		
    		this.listenerClickForm = new Button.ClickListener() {
	            @Override
	            public void buttonClick(ClickEvent event) {
	            	Notification.show("Imprimir Ficha de Avalia��o", "Selecione uma banca para imprimir a ficha de avalia��o.", Notification.Type.WARNING_MESSAGE);
	            }
	        };
	        
	        this.listenerClickTerm = new Button.ClickListener() {
	            @Override
	            public void buttonClick(ClickEvent event) {
	            	Notification.show("Imprimir Termo de Aprova��o", "Selecione uma banca para imprimir o termo de aprova��o.", Notification.Type.WARNING_MESSAGE);
	            }
	        };
	        
	        this.listenerClickFile = new Button.ClickListener() {
	            @Override
	            public void buttonClick(ClickEvent event) {
	            	Notification.show("Download do Trabalho", "Selecione uma banca para baixar o trabalho.", Notification.Type.WARNING_MESSAGE);
	            }
	        };
    		
    		this.buttonForm.addClickListener(this.listenerClickForm);
    		this.buttonTerm.addClickListener(this.listenerClickTerm);
    		this.buttonFile.addClickListener(this.listenerClickFile);
    	}
	}
	
	@Override
	public void addClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editClick(Object id) {
		try {
			JuryBO bo = new JuryBO();
			Jury jury = bo.findById((int)id);
			
			UI.getCurrent().addWindow(new EditJuryWindow(jury, this));
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			Notification.show("Marcar Banca", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	@Override
	public void deleteClick(Object id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void filterClick() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void enter(ViewChangeEvent event){
		if((event.getParameters() != null) && (!event.getParameters().isEmpty())){
			try{
				int i = Integer.parseInt(event.getParameters().trim());
				
				this.listAll = (i == 1);
			}catch(Exception e){
				Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
				
				this.listAll = false;
			}
		}
		
		this.configureButtons();
		
		super.enter(event);
	}

}
