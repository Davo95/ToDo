package todo;

import todo.commands.Commands;
import todo.menager.ToDoManager;
import todo.menager.UserManager;
import todo.model.ToDo;
import todo.model.ToDoStatus;
import todo.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ToDoMain implements Commands {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Scanner scanner = new Scanner(System.in);
    private static UserManager userManager = new UserManager();
    private static ToDoManager toDoManager = new ToDoManager();
    private static User currentUser = null;

    public static void main(String[] args) {
        boolean isRun = true;
        while (isRun) {
            Commands.printUser();
            int command;
            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                command = -1;
            }
            switch (command) {
                case EXIT:
                    isRun = false;
                    break;
                case LOGIN:
                    loginUser();
                    break;
                case REGISTER:
                    registerUser();
                    break;
                default:
                    System.out.println("Wrong command!");

            }

        }
    }

    private static void loginUser() {
        System.out.println("Please input email, password");
        try {
            String userStr = scanner.nextLine();
            String[] userStrArray = userStr.split(",");
            User user = userManager.getByEmailAndPassword(userStrArray[0], userStrArray[1]);
            if (user != null) {
                currentUser = user;
                login();
            } else {
                System.out.println("Wrong email or password");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Wrong Data!");
        }

    }

    private static void registerUser() {
        System.out.println("Please input name, surname, email, password");
        try {
            String userStr = scanner.nextLine();
            String[] userStrArray = userStr.split(",");
            User userFromManager = userManager.getByEmail(userStrArray[2]);
            if (userFromManager == null) {
                User user = new User();
                user.setName(userStrArray[0]);
                user.setSurname(userStrArray[1]);
                user.setEmail(userStrArray[2]);
                user.setPassword(userStrArray[3]);
                if (userManager.register(user)) {
                    System.out.println("User was successfully added");
                } else {
                    System.out.println("Something went wrong!");
                }
            } else {
                System.out.println("User already exists!");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Wrong data!");
        }


    }

    private static void login() {
        boolean isLogin = true;
        while (isLogin) {
            Commands.printToDo();
            int commands;
            try {
                commands = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
                commands = -1;
            }
            switch (commands) {
                case LOGOUT:
                    currentUser = null;
                    isLogin = false;
                    break;
                case ADD_NEW_TODO:
                    addNewTodo();
                    break;
                case MY_ALL_LIST:
                  printToDos(toDoManager.getAllToDosByUser(currentUser.getId()));
                    break;
                case MY_TODO_LIST:
                   printToDos(toDoManager.getAllToDosByUserAndStatus(currentUser.getId(), ToDoStatus.TODO));
                    break;
                case MY_IN_PROGRESS_LIST:
                    printToDos(toDoManager.getAllToDosByUserAndStatus(currentUser.getId(), ToDoStatus.IN_PROGRESS));
                    break;
                case MY_FINISHED_LIST:
                    printToDos(toDoManager.getAllToDosByUserAndStatus(currentUser.getId(), ToDoStatus.FINISHED));
                    break;
                case CHANGE_TODO_STATUS:
                    changeToDoStatus();
                    break;
                case DELETE_TODO:
                    deleteToDo();
                    break;
                default:
                    System.out.println("Wrong command!");
            }
        }
    }

    private static void printToDos(List<ToDo> allToDosByUser) {
        for (ToDo toDo : allToDosByUser) {
            System.out.println(toDo);
        }
    }

    private static void addNewTodo() {
        System.out.println("Please input title, deadline(yyyy-MM-dd HH:mm:ss)");
        String toDoDataStr = scanner.nextLine();
        String[] split = toDoDataStr.split(",");
        ToDo toDo = new ToDo();
        try {
            String title = split[0];
            toDo.setTitle(title);
            try {
                if (split[1] != null) {
                    toDo.setDeadline(sdf.parse(split[1]));
                }
            } catch (IndexOutOfBoundsException e) {

            } catch (ParseException e) {
                System.out.println("Please input date by this format: yyyy-MM-dd HH:mm:ss");
            }
            toDo.setStatus(ToDoStatus.TODO);
            toDo.setUser(currentUser);
            if (toDoManager.create(toDo)) {
                System.out.println("ToDo was added");
            } else {
                System.out.println("Something went wrong");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Wrong data");
        }


    }

    private static void deleteToDo() {
        System.out.println("Please choose from list:");
        List<ToDo> lint = toDoManager.getAllToDosByUser(currentUser.getId());
        for (ToDo toDo : lint) {
            System.out.println(toDo);
        }
        long id = Long.parseLong(scanner.nextLine());
        ToDo byId = toDoManager.getById(id);
        if (byId.getUser().getId() == currentUser.getId()) {
            toDoManager.delete(id);
        } else {
            System.out.println("Wrong id");
        }
    }

    private static void changeToDoStatus() {
        System.out.println("Please choose from list:");
        List<ToDo> lint = toDoManager.getAllToDosByUser(currentUser.getId());
        for (ToDo toDo : lint) {
            System.out.println(toDo);
        }
        long id = Long.parseLong(scanner.nextLine());
        ToDo byId = toDoManager.getById(id);
        if (byId.getUser().getId() == currentUser.getId()) {
            System.out.println("Please choose Status");
            System.out.println(Arrays.toString(ToDoStatus.values()));
            ToDoStatus status =  ToDoStatus.valueOf(scanner.nextLine());
            if (toDoManager.update(id,status)) {
                System.out.println("Status was changed");
            }else {
                System.out.println("Something went wrong");
            }
        } else {
            System.out.println("Wrong id");
        }
    }
}
