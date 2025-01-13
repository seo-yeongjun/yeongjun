package com.yeongjun.yeongjun.hyerin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hyerin")
@PreAuthorize("hasAnyAuthority('ADMIN', 'HYERIN')")
public class hyerinController {
    @GetMapping({"", "/"})
    public String hyerin() {
        return "hyerin/hyerin";
    }

    @GetMapping("DrawingAnimation")
    public String DrawingAnimation() {
        return "hyerin/DrawingAnimation";
    }

    @GetMapping("Interview")
    public String Interview() {
        return "hyerin/Interview";
    }

    @GetMapping("DigitalDrawingI")
    public String digitalDrawing1() {
        return "hyerin/DigitalDrawingI";
    }

    @GetMapping("DigitalDrawingII")
    public String digitalDrawing2() {
        return "hyerin/DigitalDrawingII";
    }

    @GetMapping("PhotoEssay")
    public String PhotoEssay() {
        return "hyerin/PhotoEssay";
    }

    @GetMapping("Storyboard")
    public String Storyboard() {
        return "hyerin/Storyboard";
    }

    @GetMapping("ShortMovieI")
    public String ShortMovieI() {
        return "hyerin/ShortMovieI";
    }

    @GetMapping("ShortMovieII")
    public String ShortMovieII() {
        return "hyerin/ShortMovieII";
    }

    @GetMapping("ProjectProposal")
    public String ProjectProposal() {
        return "hyerin/ProjectProposal";
    }

    @GetMapping("BusinessPlan")
    public String BusinessPlan() {
        return "hyerin/BusinessPlan";
    }

    @GetMapping("Song1")
    public String Song1() {
        return "hyerin/Song1";
    }

    @GetMapping("Song2")
    public String Song2() {
        return "hyerin/Song2";
    }
}
