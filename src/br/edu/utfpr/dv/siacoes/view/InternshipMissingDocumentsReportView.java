package br.edu.utfpr.dv.siacoes.view;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

import br.edu.utfpr.dv.siacoes.Session;
import br.edu.utfpr.dv.siacoes.bo.InternshipBO;
import br.edu.utfpr.dv.siacoes.components.CompanyComboBox;
import br.edu.utfpr.dv.siacoes.components.ProfessorComboBox;
import br.edu.utfpr.dv.siacoes.components.StudentComboBox;
import br.edu.utfpr.dv.siacoes.components.YearField;
import br.edu.utfpr.dv.siacoes.model.Internship.InternshipStatus;
import br.edu.utfpr.dv.siacoes.model.Internship.InternshipType;
import br.edu.utfpr.dv.siacoes.model.Module.SystemModule;
import br.edu.utfpr.dv.siacoes.model.User.UserProfile;

public class InternshipMissingDocumentsReportView extends ReportView {
	
	public static final String NAME = "internshipmissingdocumentsreport";

	private final YearField textYear;
	private final StudentComboBox comboStudent;
	private final ProfessorComboBox comboProfessor;
	private final CompanyComboBox comboCompany;
	private final NativeSelect comboStatus;
	private final NativeSelect comboType;
	private final CheckBox checkFinalReportMissing;
	
	public InternshipMissingDocumentsReportView(){
		super(SystemModule.SIGES);
		this.setProfilePerimissions(UserProfile.MANAGER);
		this.setCaption("Relat�rio de Documentos de Est�gio Faltantes");
		
		this.textYear = new YearField();
		this.textYear.setYear(0);
		
		this.comboStudent = new StudentComboBox("Aluno");
		
		this.comboProfessor = new ProfessorComboBox("Orientador");
		
		this.comboCompany = new CompanyComboBox();
		
		this.comboStatus = new NativeSelect("Situa��o");
		this.comboStatus.setWidth("195px");
		this.comboStatus.setNullSelectionAllowed(false);
		this.comboStatus.addItem(InternshipStatus.CURRENT);
		this.comboStatus.addItem(InternshipStatus.FINISHED);
		this.comboStatus.addItem("Todos");
		this.comboStatus.select(InternshipStatus.CURRENT);
		
		this.comboType = new NativeSelect("Tipo");
		this.comboType.setWidth("195px");
		this.comboType.setNullSelectionAllowed(false);
		this.comboType.addItem(InternshipType.NONREQUIRED);
		this.comboType.addItem(InternshipType.REQUIRED);
		this.comboType.addItem("Todos");
		this.comboType.select("Todos");
		
		this.checkFinalReportMissing = new CheckBox("Est�gio sem Relat�rio Final");
		
		HorizontalLayout h2 = new HorizontalLayout(this.textYear, this.checkFinalReportMissing);
		h2.setSpacing(true);
		
		VerticalLayout v1 = new VerticalLayout(this.comboStudent, this.comboCompany, h2);
		v1.setSpacing(true);
		
		HorizontalLayout h1 = new HorizontalLayout(this.comboType, this.comboStatus);
		h1.setSpacing(true);
		
		VerticalLayout v2 = new VerticalLayout(this.comboProfessor, h1);
		v2.setSpacing(true);
		
		this.addFilterField(new HorizontalLayout(v1, v2));
	}
	
	@Override
	public void generateReport() throws Exception {
		int type = -1, status = -1;
		
		if(!this.comboType.getValue().equals("Todos")){
			type = ((InternshipType)this.comboType.getValue()).getValue();
		}
		
		if(!this.comboStatus.getValue().equals("Todos")){
			status = ((InternshipStatus)this.comboStatus.getValue()).getValue();
		}
		
		InternshipBO bo = new InternshipBO();
		byte[] report = bo.getMissingDocumentsReport(Session.getUser().getDepartment().getIdDepartment(), this.textYear.getYear(), (this.comboStudent.getStudent() == null ? 0 : this.comboStudent.getStudent().getIdUser()), (this.comboProfessor.getProfessor() == null ? 0 : this.comboProfessor.getProfessor().getIdUser()), (this.comboCompany.getCompany() == null ? 0 : this.comboCompany.getCompany().getIdCompany()), type, status, this.checkFinalReportMissing.getValue());
		
		this.showReport(report);
	}

}
