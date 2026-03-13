package projects.service;

import java.math.BigDecimal;
import java.util.List;

import projects.dao.ProjectDao;
import projects.entity.Project;

public class ProjectService {

    private ProjectDao projectDao = new ProjectDao();

    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    public void addMaterial(Integer projectId, String name, Integer qty, BigDecimal cost) {
        projectDao.insertMaterial(projectId, name, qty, cost);
    }

    public void addStep(Integer projectId, String text, Integer order) {
        projectDao.insertStep(projectId, text, order);
    }

    public void addCategory(Integer projectId, String name) {
        projectDao.insertCategory(projectId, name);
    }
    
    public List<String> fetchMaterialsByProjectId(Integer projectId) {
        return projectDao.fetchMaterialsByProjectId(projectId);
    }

    public List<String> fetchStepsByProjectId(Integer projectId) {
        return projectDao.fetchStepsByProjectId(projectId);
    }

    public List<String> fetchCategoriesByProjectId(Integer projectId) {
        return projectDao.fetchCategoriesByProjectId(projectId);
    }

    public List<Project> fetchProjects() {
        return projectDao.fetchProjects();
    }

    public Project fetchProjectById(Integer projectId) {
        return projectDao.fetchProjectById(projectId);
    }

    public void modifyProject(Project project) {
        projectDao.updateProject(project);
    }

    public void deleteProject(Integer projectId) {
        projectDao.deleteProject(projectId);
    }
}
