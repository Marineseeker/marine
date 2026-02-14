package com.marine.manage.Controller;

import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.pojo.KnowledgeBase;
import com.marine.manage.pojo.Result;
import com.marine.manage.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

  private final KnowledgeBaseService knowledgeBaseService;

  /**
   * 添加知识条目
   */
  @PostMapping("/add")
  public Result<String> addKnowledge(@RequestBody KnowledgeBase knowledgeBase) {
    // 验证登录状态
    if (!StpUtil.isLogin()) {
      return Result.error("请先登录");
    }

    // 设置创建者
    knowledgeBase.setCreator(StpUtil.getLoginIdAsString());

    if (knowledgeBaseService.addKnowledge(knowledgeBase)) {
      log.info("添加知识条目成功: {}", knowledgeBase.getTitle());
      return Result.success("添加成功");
    } else {
      return Result.error("添加失败");
    }
  }

  /**
   * 更新知识条目
   */
  @PutMapping("/update")
  public Result<String> updateKnowledge(@RequestBody KnowledgeBase knowledgeBase) {
    if (!StpUtil.isLogin()) {
      return Result.error("请先登录");
    }

    if (knowledgeBaseService.updateKnowledge(knowledgeBase)) {
      log.info("更新知识条目成功: {}", knowledgeBase.getTitle());
      return Result.success("更新成功");
    } else {
      return Result.error("更新失败");
    }
  }

  /**
   * 删除知识条目
   */
  @DeleteMapping("/delete/{id}")
  public Result<String> deleteKnowledge(@PathVariable Long id) {
    if (!StpUtil.isLogin()) {
      return Result.error("请先登录");
    }

    if (knowledgeBaseService.deleteKnowledge(id)) {
      log.info("删除知识条目成功，ID: {}", id);
      return Result.success("删除成功");
    } else {
      return Result.error("删除失败");
    }
  }

  /**
   * 查询所有启用的知识条目
   */
  @GetMapping("/list")
  public Result<List<KnowledgeBase>> getAllKnowledge() {
    List<KnowledgeBase> knowledgeList = knowledgeBaseService.getAllEnabled();
    return Result.success(knowledgeList);
  }

  /**
   * 分页查询知识条目
   */
  @GetMapping("/page")
  public Result<PageResult<KnowledgeBase>> getKnowledgeByPage(
          @RequestParam(defaultValue = "1") int page,
          @RequestParam(defaultValue = "10") int size) {

    List<KnowledgeBase> knowledgeList = knowledgeBaseService.getKnowledgeByPage(page, size);
    int total = knowledgeBaseService.getTotalCount();

    PageResult<KnowledgeBase> pageResult = new PageResult<>();
    pageResult.setData(knowledgeList);
    pageResult.setTotal(total);
    pageResult.setPage(page);
    pageResult.setSize(size);

    return Result.success(pageResult);
  }

  /**
   * 搜索知识条目
   */
  @GetMapping("/search")
  public Result<List<KnowledgeBase>> searchKnowledge(@RequestParam String keyword) {
    List<KnowledgeBase> knowledgeList = knowledgeBaseService.searchRelevantKnowledge(keyword);
    return Result.success(knowledgeList);
  }

  /**
   * 批量导入知识条目
   */
  @PostMapping("/batch-import")
  public Result<String> batchImportKnowledge(@RequestBody List<KnowledgeBase> knowledgeList) {
    if (!StpUtil.isLogin()) {
      return Result.error("请先登录");
    }

    String creator = StpUtil.getLoginIdAsString();
    int successCount = 0;

    for (KnowledgeBase kb : knowledgeList) {
      kb.setCreator(creator);
      if (knowledgeBaseService.addKnowledge(kb)) {
        successCount++;
      }
    }

    log.info("批量导入知识条目，成功: {}/{}", successCount, knowledgeList.size());
    return Result.success(String.format("成功导入 %d/%d 条记录", successCount, knowledgeList.size()));
  }

  /**
   * 分页结果封装类
   */
  public static class PageResult<T> {
    private List<T> data;
    private int total;
    private int page;
    private int size;

    public List<T> getData() {
      return data;
    }

    public void setData(List<T> data) {
      this.data = data;
    }

    public int getTotal() {
      return total;
    }

    public void setTotal(int total) {
      this.total = total;
    }

    public int getPage() {
      return page;
    }

    public void setPage(int page) {
      this.page = page;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }
  }
}
