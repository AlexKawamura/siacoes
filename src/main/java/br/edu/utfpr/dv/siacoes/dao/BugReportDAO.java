package br.edu.utfpr.dv.siacoes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.dv.siacoes.model.BugReport;
import br.edu.utfpr.dv.siacoes.model.BugReport.BugStatus;
import br.edu.utfpr.dv.siacoes.model.User;
import br.edu.utfpr.dv.siacoes.model.Module;

public class BugReportDAO {
	
        // Removido finally com método close() e substituido por try-with-resource.
	public BugReport findById(int id) throws SQLException{
                String sql = "SELECT bugreport.*, \"user\".name " + 
                            "FROM bugreport INNER JOIN \"user\" ON \"user\".idUser=bugreport.idUser " +
                            "WHERE idBugReport = ?";
		
		try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql);
                ){
			stmt.setInt(1, id);
			
			try(ResultSet rs = stmt.executeQuery()){
                                if(rs.next()){
                                        return this.loadObject(rs);
                                }else{
                                        return null;
                                }
                        }
		}
	}
	
        // Removido finally com método close() e substituido por try-with-resource.
	public List<BugReport> listAll() throws SQLException{
                String sql = "SELECT bugreport.*, \"user\".name " +
                            "FROM bugreport INNER JOIN \"user\" ON \"user\".idUser=bugreport.idUser " +
                            "ORDER BY status, reportdate";
            
		try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
			Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                ){
			List<BugReport> list = new ArrayList<BugReport>();
			
			while(rs.next()){
				list.add(this.loadObject(rs));
			}
			
			return list;
		}
	}
	
        // Separando insert e update em duas funções, diminuindo quantidade de if-else.
        // Podendo melhorar o entendimento do que será feito pelo código.
	public int save(BugReport bug) throws SQLException{
		boolean insert = (bug.getIdBugReport() == 0);
                
		if(insert){
			return insert(bug);
		}else{
			return update(bug);
		}
	}
        
        // Criado método para realizar apenas o INSERT.
        // Removido finally com método close() e substituido por try-with-resource.
        private int insert(BugReport bug) throws SQLException{
                String sql = "INSERT INTO bugreport(idUser, module, title, description, reportDate, type, status, statusDate, statusDescription) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ){
			stmt.setInt(1, bug.getUser().getIdUser());
			stmt.setInt(2, bug.getModule().getValue());
			stmt.setString(3, bug.getTitle());
			stmt.setString(4, bug.getDescription());
			stmt.setDate(5, new java.sql.Date(bug.getReportDate().getTime()));
			stmt.setInt(6, bug.getType().getValue());
			stmt.setInt(7, bug.getStatus().getValue());
			if(bug.getStatus() == BugStatus.REPORTED){
				stmt.setNull(8, Types.DATE);
			}else{
				stmt.setDate(8, new java.sql.Date(bug.getStatusDate().getTime()));
			}
			stmt.setString(9, bug.getStatusDescription());
			
			stmt.execute();

			try(ResultSet rs = stmt.getGeneratedKeys()){
                                if(rs.next()){
                                        bug.setIdBugReport(rs.getInt(1));
                                }

                                return bug.getIdBugReport();
                        }
		}
	}
        
        // Criado método para realizar apenas o UPDATE.
        // Removido finally com método close() e substituido por try-with-resource.
        private int update(BugReport bug) throws SQLException{
                String sql = "UPDATE bugreport SET idUser=?, module=?, title=?, description=?, reportDate=?, type=?, status=?, statusDate=?, statusDescription=? WHERE idBugReport=?";
		
		try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql);
                ){
			stmt.setInt(1, bug.getUser().getIdUser());
			stmt.setInt(2, bug.getModule().getValue());
			stmt.setString(3, bug.getTitle());
			stmt.setString(4, bug.getDescription());
			stmt.setDate(5, new java.sql.Date(bug.getReportDate().getTime()));
			stmt.setInt(6, bug.getType().getValue());
			stmt.setInt(7, bug.getStatus().getValue());
			if(bug.getStatus() == BugStatus.REPORTED){
				stmt.setNull(8, Types.DATE);
			}else{
				stmt.setDate(8, new java.sql.Date(bug.getStatusDate().getTime()));
			}
			stmt.setString(9, bug.getStatusDescription());
			stmt.setInt(10, bug.getIdBugReport());
			
			stmt.execute();
			
			return bug.getIdBugReport();
		}
	}
	
	private BugReport loadObject(ResultSet rs) throws SQLException{
		BugReport bug = new BugReport();
		
		bug.setIdBugReport(rs.getInt("idBugReport"));
		bug.setUser(new User());
		bug.getUser().setIdUser(rs.getInt("idUser"));
		bug.getUser().setName(rs.getString("name"));
		bug.setModule(Module.SystemModule.valueOf(rs.getInt("module")));
		bug.setTitle(rs.getString("title"));
		bug.setDescription(rs.getString("description"));
		bug.setReportDate(rs.getDate("reportDate"));
		bug.setType(BugReport.BugType.valueOf(rs.getInt("type")));
		bug.setStatus(BugReport.BugStatus.valueOf(rs.getInt("status")));
		bug.setStatusDate(rs.getDate("statusDate"));
		bug.setStatusDescription(rs.getString("statusDescription"));
		
		return bug;
	}

}
