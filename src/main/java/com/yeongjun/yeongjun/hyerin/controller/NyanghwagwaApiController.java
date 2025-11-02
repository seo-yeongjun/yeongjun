package com.yeongjun.yeongjun.hyerin.controller;

import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaDashboardView;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaItemUpsertRequest;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaProduceRequest;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSaleRequest;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSetComponentRequest;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSetUpsertRequest;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSetViewDto;
import com.yeongjun.yeongjun.hyerin.service.NyanghwagwaInventoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/hyerin/nyanghwagwa")
@PreAuthorize("hasAnyAuthority('ADMIN', 'HYERIN')")
public class NyanghwagwaApiController {

    private final NyanghwagwaInventoryService inventoryService;

    public NyanghwagwaApiController(NyanghwagwaInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/dashboard")
    public NyanghwagwaDashboardView getDashboard() {
        return inventoryService.loadDashboardView();
    }

    @GetMapping("/sets/{setId}")
    public NyanghwagwaSetViewDto getSet(@PathVariable Long setId) {
        return inventoryService.getSetView(setId);
    }

    @PostMapping("/produce")
    public NyanghwagwaDashboardView produce(@Valid @RequestBody NyanghwagwaProduceRequest request,
                                            @AuthenticationPrincipal User user) {
        if (request.getSetId() != null) {
            inventoryService.produceSet(request.getSetId(), request.getCount(), resolveActor(user), request.getNotes());
        } else if (request.getItemId() != null) {
            inventoryService.produceItem(request.getItemId(), request.getCount(), resolveActor(user), request.getNotes());
        } else {
            throw new IllegalArgumentException("세트 ID 또는 아이템 ID 중 하나는 필수입니다.");
        }
        return inventoryService.loadDashboardView();
    }

    @PostMapping("/sale")
    public NyanghwagwaDashboardView sale(@Valid @RequestBody NyanghwagwaSaleRequest request,
                                         @AuthenticationPrincipal User user) {
        inventoryService.sellSet(request.getSetId(), request.getCount(), resolveActor(user), request.getNotes());
        return inventoryService.loadDashboardView();
    }

    @PostMapping("/items")
    public NyanghwagwaDashboardView createItem(@Valid @RequestBody NyanghwagwaItemUpsertRequest request) {
        inventoryService.createItem(request.getItemName(), request.getQuantity(), request.getUnit(), request.getDescription());
        return inventoryService.loadDashboardView();
    }

    @PutMapping("/items/{itemId}")
    public NyanghwagwaDashboardView updateItem(@PathVariable Long itemId,
                                               @Valid @RequestBody NyanghwagwaItemUpsertRequest request) {
        inventoryService.updateItem(itemId, request.getItemName(), request.getQuantity(), request.getUnit(), request.getDescription());
        return inventoryService.loadDashboardView();
    }

    @DeleteMapping("/items/{itemId}")
    public NyanghwagwaDashboardView deleteItem(@PathVariable Long itemId) {
        inventoryService.deleteItem(itemId);
        return inventoryService.loadDashboardView();
    }

    @PostMapping("/sets")
    public NyanghwagwaDashboardView createSet(@Valid @RequestBody NyanghwagwaSetUpsertRequest request) {
        inventoryService.upsertSet(null, request.getSetName(), request.getDescription(), toInputs(request.getComponents()));
        return inventoryService.loadDashboardView();
    }

    @PutMapping("/sets/{setId}")
    public NyanghwagwaDashboardView updateSet(@PathVariable Long setId,
                                              @Valid @RequestBody NyanghwagwaSetUpsertRequest request) {
        inventoryService.upsertSet(setId, request.getSetName(), request.getDescription(), toInputs(request.getComponents()));
        return inventoryService.loadDashboardView();
    }

    @DeleteMapping("/sets/{setId}")
    public NyanghwagwaDashboardView deleteSet(@PathVariable Long setId) {
        inventoryService.deleteSet(setId);
        return inventoryService.loadDashboardView();
    }

    private List<NyanghwagwaInventoryService.SetComponentInput> toInputs(List<NyanghwagwaSetComponentRequest> components) {
        if (components == null || components.isEmpty()) {
            return Collections.emptyList();
        }
        return components.stream()
                .map(component -> new NyanghwagwaInventoryService.SetComponentInput(component.getItemId(), component.getRequiredQuantity()))
                .toList();
    }

    private String resolveActor(User user) {
        return user != null ? user.getUsername() : "anonymous";
    }
}
