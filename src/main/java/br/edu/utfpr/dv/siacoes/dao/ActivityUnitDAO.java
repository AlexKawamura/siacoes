package br.edu.utfpr.dv.siacoes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.dv.siacoes.log.UpdateEvent;
import br.edu.utfpr.dv.siacoes.model.ActivityUnit;

public class ActivityUnitDAO {
	
	public List<ActivityUnit> listAll() throws SQLException{
            
		try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
			Statement stmt = conn.createStatement();
                ){
                        String sql = "SELECT * FROM activityunit ORDER BY description";
                        
                        try(ResultSet rs = stmt.executeQuery(sql)){

                                List<ActivityUnit> list = new ArrayList<ActivityUnit>();

                                while(rs.next()){
                                        list.add(this.loadObject(rs));
                                }

                                return list;
                        }
		}
	}
	
	public ActivityUnit findById(int id) throws SQLException{
                String sql = "SELECT * FROM activityunit WHERE idActivityUnit=?";
            
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
	
	public int save(int idUser, ActivityUnit unit){
		boolean insert = (unit.getIdActivityUnit() == 0);
                	
                if(insert){
                        return insert(idUser, unit);
                } else {
                        return update(idUser, unit);
		}
	}
        
        private int insert(int idUser, ActivityUnit unit) throws SQLException {
                String sql = "INSERT INTO activityunit(description, fillAmount, amountDescription) VALUES(?, ?, ?)";
            
                try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ){
                        stmt.setString(1, unit.getDescription());
                        stmt.setInt(2, (unit.isFillAmount() ? 1 : 0));
                        stmt.setString(3, unit.getAmountDescription());

                        stmt.execute();
                                
                        try(ResultSet rs = stmt.getGeneratedKeys()){
                                if(rs.next()){
                                        unit.setIdActivityUnit(rs.getInt(1));
                                }

                                new UpdateEvent(conn).registerInsert(idUser, unit);

                                return unit.getIdActivityUnit();
                        }
                }
        }
        
        private int update(int idUser, ActivityUnit unit) throws SQLException {
                String sql = "UPDATE activityunit SET description=?, fillAmount=?, amountDescription=? WHERE idActivityUnit=?";
            
                try(
                        Connection conn = ConnectionDAO.getInstance().getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql);
                ){
                        stmt.setString(1, unit.getDescription());
			stmt.setInt(2, (unit.isFillAmount() ? 1 : 0));
			stmt.setString(3, unit.getAmountDescription());
			stmt.setInt(4, unit.getIdActivityUnit());
			
                        stmt.execute();
                        
                        new UpdateEvent(conn).registerUpdate(idUser, unit);
                        
                        return unit.getIdActivityUnit();
                }
        }
	
	private ActivityUnit loadObject(ResultSet rs) throws SQLException{
		ActivityUnit unit = new ActivityUnit();
		
		unit.setIdActivityUnit(rs.getInt("idActivityUnit"));
		unit.setDescription(rs.getString("Description"));
		unit.setFillAmount(rs.getInt("fillAmount") == 1);
		unit.setAmountDescription(rs.getString("amountDescription"));
		
		return unit;
	}

}
