package com.yeongjun.yeongjun.hyerin.repository;

import com.yeongjun.yeongjun.hyerin.entity.HyerinPortfolioPage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class HyerinRepository {

    private final List<HyerinPortfolioPage> pages;
    private final Map<String, HyerinPortfolioPage> pageIndex;

    public HyerinRepository() {
        this.pages = List.of(
                new HyerinPortfolioPage("DrawingAnimation", "hyerin/DrawingAnimation", "Drawing Animation"),
                new HyerinPortfolioPage("Interview", "hyerin/Interview", "Interview"),
                new HyerinPortfolioPage("DigitalDrawingI", "hyerin/DigitalDrawingI", "Digital Drawing I"),
                new HyerinPortfolioPage("DigitalDrawingII", "hyerin/DigitalDrawingII", "Digital Drawing II"),
                new HyerinPortfolioPage("PhotoEssay", "hyerin/PhotoEssay", "Photo Essay"),
                new HyerinPortfolioPage("Storyboard", "hyerin/Storyboard", "Storyboard"),
                new HyerinPortfolioPage("ShortMovieI", "hyerin/ShortMovieI", "Short Movie I"),
                new HyerinPortfolioPage("ShortMovieII", "hyerin/ShortMovieII", "Short Movie II"),
                new HyerinPortfolioPage("ProjectProposal", "hyerin/ProjectProposal", "Project Proposal"),
                new HyerinPortfolioPage("BusinessPlan", "hyerin/BusinessPlan", "Business Plan"),
                new HyerinPortfolioPage("Song1", "hyerin/Song1", "Song 1"),
                new HyerinPortfolioPage("Song2", "hyerin/Song2", "Song 2"),
                new HyerinPortfolioPage("nyanghwagwa", "hyerin/nyanghwagwa", "냥화과 재고관리")
        );

        this.pageIndex = pages.stream()
                .collect(Collectors.toUnmodifiableMap(page -> normalize(page.getKey()), Function.identity()));
    }

    public Optional<HyerinPortfolioPage> findByKey(String key) {
        return Optional.ofNullable(pageIndex.get(normalize(key)));
    }

    public List<HyerinPortfolioPage> findAll() {
        return pages;
    }

    private String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
