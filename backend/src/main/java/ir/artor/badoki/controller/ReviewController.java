package ir.artor.badoki.controller;

import ir.artor.badoki.dto.ReviewRequest;
import ir.artor.badoki.dto.ReviewResponse;
import ir.artor.badoki.model.User;
import ir.artor.badoki.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** امتیاز و نظر بیماران درباره پزشکان */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** فهرست نظرات پزشک — عمومی */
    @GetMapping("/doctors/{id}/reviews")
    public List<ReviewResponse> list(@PathVariable Long id) {
        return reviewService.listForDoctor(id);
    }

    /** ثبت نظر — نیاز به ورود (فقط با نوبت انجام‌شده) */
    @PostMapping("/reviews")
    public ReviewResponse create(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(user, request);
    }

    /** حذف نظر خود — فقط صاحب نظر */
    @DeleteMapping("/reviews/{id}")
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        reviewService.delete(user, id);
    }
}
