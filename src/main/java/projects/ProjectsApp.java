package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

// Menu driven application that performs CRUD operations on the projects table
public class ProjectsApp {

    private Scanner scanner = new Scanner(System.in);
    private ProjectService projectService = new ProjectService();
    private Project curProject;

    // Menu options
    private List<String> operations = List.of(
        "1) Add a project",
        "2) List projects",
        "3) Select a project",
        "4) Update a project",
        "5) Delete a project"
    );

    // Entry point
    public static void main(String[] args) {
        new ProjectsApp().processUserSelections();
    }

    private void processUserSelections() {

        boolean done = false;

        while (!done) {

            try {

                int selection = getUserSelection();

                switch (selection) {

                case -1:
                    done = exitMenu();
                    break;

                case 1:
                    createProject();
                    break;

                case 2:
                    listProjects();
                    break;

                case 3:
                    selectProject();
                    break;

                case 4:
                    updateProject();
                    break;

                case 5:
                    deleteProject();
                    break;

                default:
                    System.out.println("\n" + selection + " is not valid. Try again.");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Create project
    private void createProject() {

        String projectName = getStringInput("Enter the project name");
        BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
        BigDecimal actualHours = getDecimalInput("Enter the actual hours");
        Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
        String notes = getStringInput("Enter the project notes");

        Project project = new Project();

        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        Project dbProject = projectService.addProject(project);

        System.out.println("You successfully created project: " + dbProject);

        addMaterials(dbProject);
        addSteps(dbProject);
        addCategories(dbProject);
    }

    private void addMaterials(Project project) {

        String add = getStringInput("Add materials to this project? (y/n)");

        while (add != null && add.equalsIgnoreCase("y")) {

            String name = getStringInput("Enter material name");
            Integer qty = getIntInput("Enter quantity required");
            BigDecimal cost = getDecimalInput("Enter material cost");

            projectService.addMaterial(project.getProjectId(), name, qty, cost);

            add = getStringInput("Add another material? (y/n)");
        }
    }

    private void addSteps(Project project) {

        String add = getStringInput("Add steps to this project? (y/n)");

        int order = 1;

        while (add != null && add.equalsIgnoreCase("y")) {

            String text = getStringInput("Enter step description");

            projectService.addStep(project.getProjectId(), text, order);

            order++;

            add = getStringInput("Add another step? (y/n)");
        }
    }

    private void addCategories(Project project) {

        String add = getStringInput("Add categories to this project? (y/n)");

        while (add != null && add.equalsIgnoreCase("y")) {

            String categoryName = getStringInput("Enter category name");

            projectService.addCategory(project.getProjectId(), categoryName);

            add = getStringInput("Add another category? (y/n)");
        }
    }

    // List projects
    private void listProjects() {

        List<Project> projects = projectService.fetchProjects();

        for (Project project : projects) {
            System.out.println(project);
        }
    }

    // Select a specific project
    private void selectProject() {

        listProjects();

        Integer projectId = getIntInput("Enter a project ID to select a project");

        curProject = projectService.fetchProjectById(projectId);

        if (Objects.isNull(curProject)) {
            throw new DbException("Project ID " + projectId + " does not exist.");
        }

        System.out.println("\nSelected project: " + curProject);

        // Print Materials
        System.out.println("\nMaterials:");
        projectService.fetchMaterialsByProjectId(projectId)
            .forEach(material -> System.out.println(material));

        // Print Steps
        System.out.println("\nSteps:");
        projectService.fetchStepsByProjectId(projectId)
            .forEach(step -> System.out.println(step));

        // Print Categories
        System.out.println("\nCategories:");
        projectService.fetchCategoriesByProjectId(projectId)
            .forEach(category -> System.out.println(category));
    }

    // Update project
    private void updateProject() {

        if (Objects.isNull(curProject)) {
            throw new DbException("You must select a project before updating.");
        }

        String projectName = getStringInput("Enter the new project name");
        BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
        BigDecimal actualHours = getDecimalInput("Enter the actual hours");
        Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
        String notes = getStringInput("Enter the project notes");

        Project project = new Project();
        project.setProjectId(curProject.getProjectId());
        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        projectService.modifyProject(project);

        System.out.println("Project updated successfully.");

        curProject = projectService.fetchProjectById(project.getProjectId());
    }

    // Delete project
    private void deleteProject() {

        List<Project> projects = projectService.fetchProjects();

        if (projects.isEmpty()) {
            throw new DbException("No projects available to delete.");
        }

        projects.forEach(project -> System.out.println(project));

        Integer projectId = getIntInput("Enter the project ID to delete");

        if (Objects.isNull(projectId)) {
            throw new DbException("Project ID cannot be null.");
        }

        Project project = projectService.fetchProjectById(projectId);

        if (Objects.isNull(project)) {
            throw new DbException("Project ID " + projectId + " does not exist.");
        }

        projectService.deleteProject(projectId);

        System.out.println("Project deleted successfully.");

        if (curProject != null && curProject.getProjectId().equals(projectId)) {
            curProject = null;
        }
    }

    private BigDecimal getDecimalInput(String prompt) {

        String input = getStringInput(prompt);

        if (Objects.isNull(input)) {
            return null;
        }

        try {
            return new BigDecimal(input).setScale(2);
        } catch (NumberFormatException e) {
            throw new DbException(input + " is not a valid decimal number.");
        }
    }

    private boolean exitMenu() {

        System.out.println("Exiting the menu.");

        return true;
    }

    private int getUserSelection() {

        printOperations();

        Integer input = getIntInput("\nEnter a menu selection");

        return Objects.isNull(input) ? -1 : input;
    }

    private Integer getIntInput(String prompt) {

        String input = getStringInput(prompt);

        if (Objects.isNull(input)) {
            return null;
        }

        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            throw new DbException(input + " is not a valid number.");
        }
    }

    private String getStringInput(String prompt) {

        System.out.print(prompt + ": ");

        String input = scanner.nextLine();

        return input.isBlank() ? null : input.trim();
    }

    private void printOperations() {

        System.out.println("\nAvailable selections. Press Enter to quit:");

        operations.forEach(line -> System.out.println("  " + line));
    }
}