package projects.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import projects.entity.Project;
import projects.exception.DbException;

public class ProjectDao {

    public Project insertProject(Project project) {

        String sql = """
            INSERT INTO project
            (project_name, estimated_hours, actual_hours, difficulty, notes)
            VALUES (?, ?, ?, ?, ?)
            """;

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getProjectName());
            stmt.setBigDecimal(2, project.getEstimatedHours());
            stmt.setBigDecimal(3, project.getActualHours());
            stmt.setInt(4, project.getDifficulty());
            stmt.setString(5, project.getNotes());

            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            
            if(rs.next()) {
                project.setProjectId(rs.getInt(1));
            }

            return project;

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }

    public List<Project> fetchProjects() {

        String sql = "SELECT * FROM project";

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            List<Project> projects = new ArrayList<>();

            while(rs.next()) {

                Project project = new Project();

                project.setProjectId(rs.getInt("project_id"));
                project.setProjectName(rs.getString("project_name"));

                projects.add(project);
            }

            return projects;

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }
    
    public Project fetchProjectById(Integer projectId) {

        String sql = "SELECT * FROM project WHERE project_id = ?";

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {

                Project project = new Project();

                project.setProjectId(rs.getInt("project_id"));
                project.setProjectName(rs.getString("project_name"));
                project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
                project.setActualHours(rs.getBigDecimal("actual_hours"));
                project.setDifficulty(rs.getInt("difficulty"));
                project.setNotes(rs.getString("notes"));

                return project;
            }

            throw new DbException("Project ID not found: " + projectId);

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }

    public void updateProject(Project project) {

        String sql = """
            UPDATE project
            SET project_name = ?
            WHERE project_id = ?
            """;

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, project.getProjectName());
            stmt.setInt(2, project.getProjectId());

            stmt.executeUpdate();

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }

    public void deleteProject(Integer projectId) {

        String sql = "DELETE FROM project WHERE project_id = ?";

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            stmt.executeUpdate();

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }
    
    public void insertMaterial(Integer projectId, String name, Integer qty, BigDecimal cost) {

        String sql = """
            INSERT INTO material
            (project_id, material_name, num_required, cost)
            VALUES (?, ?, ?, ?)
            """;

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setString(2, name);
            stmt.setInt(3, qty);
            stmt.setBigDecimal(4, cost);

            stmt.executeUpdate();

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }
    
    public void insertStep(Integer projectId, String text, Integer order) {

        String sql = """
            INSERT INTO step
            (project_id, step_text, step_order)
            VALUES (?, ?, ?)
            """;

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setString(2, text);
            stmt.setInt(3, order);

            stmt.executeUpdate();

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }
    
    public void insertCategory(Integer projectId, String categoryName) {

        String categorySql = "INSERT INTO category (category_name) VALUES (?)";
        String linkSql = "INSERT INTO project_category (project_id, category_id) VALUES (?, ?)";

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement catStmt = conn.prepareStatement(categorySql, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {

            catStmt.setString(1, categoryName);
            catStmt.executeUpdate();

            ResultSet rs = catStmt.getGeneratedKeys();

            if(rs.next()) {

                int categoryId = rs.getInt(1);

                linkStmt.setInt(1, projectId);
                linkStmt.setInt(2, categoryId);

                linkStmt.executeUpdate();
            }

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }

    // ==============================
    // NEW METHODS FOR SELECT PROJECT
    // ==============================

    public List<String> fetchMaterialsByProjectId(Integer projectId) {

        String sql = "SELECT material_name, num_required, cost FROM material WHERE project_id = ?";

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            ResultSet rs = stmt.executeQuery();

            List<String> materials = new ArrayList<>();

            while(rs.next()) {

                String material =
                    rs.getString("material_name") +
                    " (Qty: " + rs.getInt("num_required") +
                    ", Cost: " + rs.getBigDecimal("cost") + ")";

                materials.add(material);
            }

            return materials;

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }

    public List<String> fetchStepsByProjectId(Integer projectId) {

        String sql = "SELECT step_order, step_text FROM step WHERE project_id = ? ORDER BY step_order";

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            ResultSet rs = stmt.executeQuery();

            List<String> steps = new ArrayList<>();

            while(rs.next()) {

                String step =
                    rs.getInt("step_order") + ". " +
                    rs.getString("step_text");

                steps.add(step);
            }

            return steps;

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }

    public List<String> fetchCategoriesByProjectId(Integer projectId) {

        String sql = """
            SELECT c.category_name
            FROM category c
            JOIN project_category pc ON c.category_id = pc.category_id
            WHERE pc.project_id = ?
            """;

        try(Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            ResultSet rs = stmt.executeQuery();

            List<String> categories = new ArrayList<>();

            while(rs.next()) {

                categories.add(rs.getString("category_name"));
            }

            return categories;

        } catch(SQLException e) {
            throw new DbException(e);
        }
    }
}