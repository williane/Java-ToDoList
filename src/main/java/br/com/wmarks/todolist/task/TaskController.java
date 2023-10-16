package br.com.wmarks.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.wmarks.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {

    var idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);

    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("As datas de início e/ou término não podem ser anteriores à atual.");
    } else if (taskModel.getEndAt().isBefore(taskModel.getStartAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de fim deve vir depois da de início.");
    }
    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest req) {
    var idUser = req.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser((UUID) idUser);
    return tasks;
  }

  @PutMapping("/{idTask}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID idTask, HttpServletRequest req) {

    var task = this.taskRepository.findById(idTask).orElse(null);

    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Tarefa não encontrada.");
    }

    var idUser = req.getAttribute("idUser");

    if (!task.getIdUser().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Você não pode editar tarefas fora de sua posse.");
    }

    LocalDateTime rightNow = LocalDateTime.now();
    task.setCreatedAt(rightNow);

    Utils.copyNonNullproperties(taskModel, task);

    var updatedTask = this.taskRepository.save(task);
    return ResponseEntity.ok().body(updatedTask);

  }
}
