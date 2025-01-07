package com.ll.security_2025_01_06.domain.post.post.controller;

import com.ll.security_2025_01_06.domain.member.member.entity.Member;
import com.ll.security_2025_01_06.domain.post.post.dto.PostDto;
import com.ll.security_2025_01_06.domain.post.post.dto.PostWithContentDto;
import com.ll.security_2025_01_06.domain.post.post.entity.Post;
import com.ll.security_2025_01_06.domain.post.post.service.PostService;
import com.ll.security_2025_01_06.global.exceptions.ServiceException;
import com.ll.security_2025_01_06.global.rq.Rq;
import com.ll.security_2025_01_06.global.rsData.RsData;
import com.ll.security_2025_01_06.standard.page.dto.PageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping
    @Transactional(readOnly = true)
    public PageDto<PostDto> items(
            @RequestParam(defaultValue = "title") String searchKeywordType,
            @RequestParam(defaultValue = "") String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return new PageDto<>(
                postService.findByListedPaged(true, searchKeywordType, searchKeyword, page, pageSize)
                        .map(PostDto::new)
        );
    }

    @GetMapping("/mine")
    @Transactional(readOnly = true)
    public PageDto<PostDto> mine(
            @RequestParam(defaultValue = "title") String searchKeywordType,
            @RequestParam(defaultValue = "") String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Member author = rq.getActor();

        return new PageDto<>(
                postService.findByAuthorPaged(author, searchKeywordType, searchKeyword, page, pageSize)
                        .map(PostDto::new)
        );
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostWithContentDto item(@PathVariable long id) {
        Post post = postService.findById(id).get();

        if(!post.isPublished()) {
            Member author = rq.getActor();

            if(author == null) {
                throw new ServiceException("401-1", "로그인이 필요합니다.");
            }

            post.checkActorCanRead(author);
        }

        return new PostWithContentDto(postService.findById(id).get());
    }

    record PostWriteReqBody(
            @NotBlank
            String title,

            @NotBlank
            String content,

            boolean published,
            boolean listed
    ) {}

    @PostMapping
    @Transactional
    public RsData<PostWithContentDto> write(
            @RequestBody @Valid PostWriteReqBody reqBody
    ) {

        Post post = postService.write(rq.getActor(), reqBody.title, reqBody.content, reqBody.published, reqBody.listed);

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    record PostModifyReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content,

            boolean published,
            boolean listed
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostWithContentDto> modify(
            @PathVariable long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Member author = rq.getActor();
        Post post = postService.findById(id).get();

        post.checkActorCanModify(author);
        postService.modify(post, reqBody.title, reqBody.content, reqBody.published, reqBody.listed);

        postService.flush();

        return new RsData<>(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> delete(@PathVariable long id) {
        Member author = rq.getActor();
        Post post = postService.findById(id).get();

        post.checkActorCanDelete(author);
        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(post.getId())
        );
    }
}