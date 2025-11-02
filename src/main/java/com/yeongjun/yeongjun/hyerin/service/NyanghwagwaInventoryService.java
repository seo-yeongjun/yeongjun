package com.yeongjun.yeongjun.hyerin.service;

import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaDashboardView;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaItemViewDto;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSetComponentDto;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSetStatusDto;
import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaSetViewDto;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaInventoryLog;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaInventoryLogType;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaItem;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaItemSet;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaItemSetEntry;
import com.yeongjun.yeongjun.hyerin.repository.NyanghwagwaInventoryLogDAO;
import com.yeongjun.yeongjun.hyerin.repository.NyanghwagwaItemDAO;
import com.yeongjun.yeongjun.hyerin.repository.NyanghwagwaItemSetDAO;
import com.yeongjun.yeongjun.hyerin.repository.NyanghwagwaItemSetEntryDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NyanghwagwaInventoryService {

    public record SetComponentInput(Long itemId, Integer requiredQuantity) {}

    private final NyanghwagwaItemDAO itemDAO;
    private final NyanghwagwaItemSetDAO itemSetDAO;
    private final NyanghwagwaItemSetEntryDAO itemSetEntryDAO;
    private final NyanghwagwaInventoryLogDAO inventoryLogDAO;

    public NyanghwagwaInventoryService(NyanghwagwaItemDAO itemDAO,
                                       NyanghwagwaItemSetDAO itemSetDAO,
                                       NyanghwagwaItemSetEntryDAO itemSetEntryDAO,
                                       NyanghwagwaInventoryLogDAO inventoryLogDAO) {
        this.itemDAO = itemDAO;
        this.itemSetDAO = itemSetDAO;
        this.itemSetEntryDAO = itemSetEntryDAO;
        this.inventoryLogDAO = inventoryLogDAO;
    }

    public NyanghwagwaDashboardView loadDashboardView() {
        List<NyanghwagwaItem> items = itemDAO.selectAllItems();
        Map<Long, NyanghwagwaItem> itemMap = toItemMap(items);

        List<NyanghwagwaItemSet> sets = itemSetDAO.selectAllSets();
        Map<Long, List<NyanghwagwaItemSetEntry>> entriesBySet = itemSetEntryDAO.selectAllEntries()
                .stream()
                .collect(Collectors.groupingBy(NyanghwagwaItemSetEntry::getSet_id));

        List<NyanghwagwaSetStatusDto> setStatuses = sets.stream()
                .map(set -> buildSetStatusDto(set, entriesBySet.get(set.getSet_id()), itemMap))
                .sorted(Comparator.comparing(NyanghwagwaSetStatusDto::getSetName))
                .toList();

        List<NyanghwagwaSetViewDto> setViewDtos = sets.stream()
                .map(set -> buildSetViewDto(set, entriesBySet.get(set.getSet_id()), itemMap))
                .sorted(Comparator.comparing(NyanghwagwaSetViewDto::getSetName))
                .toList();

        List<NyanghwagwaItemViewDto> itemViewDtos = items.stream()
                .sorted(Comparator.comparing(NyanghwagwaItem::getItem_name))
                .map(this::buildItemViewDto)
                .toList();

        return NyanghwagwaDashboardView.builder()
                .setStatuses(setStatuses)
                .produceCandidates(setViewDtos)
                .saleCandidates(setViewDtos)
                .itemList(itemViewDtos)
                .setList(setViewDtos)
                .build();
    }

    public NyanghwagwaSetViewDto getSetView(Long setId) {
        NyanghwagwaItemSet set = requireSet(setId);
        List<NyanghwagwaItemSetEntry> entries = itemSetEntryDAO.selectEntriesBySetId(setId);
        Map<Long, NyanghwagwaItem> itemMap = toItemMap(itemDAO.selectAllItems());
        return buildSetViewDto(set, entries, itemMap);
    }

    @Transactional
    public NyanghwagwaItem createItem(String itemName, Integer quantity, String unit, String description) {
        if (!StringUtils.hasText(itemName)) {
            throw new IllegalArgumentException("아이템 이름을 입력해 주세요.");
        }

        NyanghwagwaItem item = new NyanghwagwaItem();
        item.setItem_name(itemName.trim());
        item.setQuantity(quantity == null ? 0 : Math.max(0, quantity));
        item.setUnit(StringUtils.hasText(unit) ? unit.trim() : null);
        item.setDescription(StringUtils.hasText(description) ? description.trim() : null);
        itemDAO.insertItem(item);
        return requireItem(item.getItem_id());
    }

    @Transactional
    public NyanghwagwaItem updateItem(Long itemId, String itemName, Integer quantity, String unit, String description) {
        NyanghwagwaItem item = requireItem(itemId);

        if (StringUtils.hasText(itemName)) {
            item.setItem_name(itemName.trim());
        }
        if (quantity != null) {
            item.setQuantity(Math.max(0, quantity));
        }
        item.setUnit(StringUtils.hasText(unit) ? unit.trim() : null);
        item.setDescription(StringUtils.hasText(description) ? description.trim() : null);

        itemDAO.updateItem(item);
        return requireItem(itemId);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        requireItem(itemId);
        itemDAO.deleteItem(itemId);
    }

    @Transactional
    public NyanghwagwaItemSet upsertSet(Long setId,
                                        String setName,
                                        String description,
                                        List<SetComponentInput> components) {
        if (!StringUtils.hasText(setName)) {
            throw new IllegalArgumentException("세트 이름을 입력해 주세요.");
        }

        NyanghwagwaItemSet set;
        boolean isCreate = (setId == null);

        if (isCreate) {
            set = new NyanghwagwaItemSet();
        } else {
            set = requireSet(setId);
        }

        set.setSet_name(setName.trim());
        set.setDescription(StringUtils.hasText(description) ? description.trim() : null);

        if (isCreate) {
            itemSetDAO.insertSet(set);
            setId = set.getSet_id();
        } else {
            itemSetDAO.updateSet(set);
        }

        replaceSetEntries(setId, components);
        return requireSet(setId);
    }

    @Transactional
    public void deleteSet(Long setId) {
        requireSet(setId);
        itemSetEntryDAO.deleteEntriesBySetId(setId);
        itemSetDAO.deleteSet(setId);
    }

    @Transactional
    public void produceSet(Long setId, int produceCount, String actorUsername, String notes) {
        if (produceCount <= 0) {
            throw new IllegalArgumentException("제작 수량은 1 이상이어야 합니다.");
        }
        NyanghwagwaItemSet set = requireSet(setId);
        List<NyanghwagwaItemSetEntry> entries = requireEntries(setId);

        for (NyanghwagwaItemSetEntry entry : entries) {
            NyanghwagwaItem item = requireItem(entry.getItem_id());
            int delta = entry.getRequired_quantity() * produceCount;
            item.setQuantity(item.getQuantity() + delta);
            itemDAO.updateItemQuantity(item);
            insertLog(set.getSet_id(), item.getItem_id(), NyanghwagwaInventoryLogType.PRODUCE, delta,
                    item.getQuantity(), actorUsername, notes);
        }

        insertLog(set.getSet_id(), null, NyanghwagwaInventoryLogType.PRODUCE, produceCount, calculateSetStock(set.getSet_id()),
                actorUsername, notes);
    }

    @Transactional
    public void produceItem(Long itemId, int produceCount, String actorUsername, String notes) {
        if (produceCount <= 0) {
            throw new IllegalArgumentException("제작 수량은 1 이상이어야 합니다.");
        }

        NyanghwagwaItem item = requireItem(itemId);
        item.setQuantity(item.getQuantity() + produceCount);
        itemDAO.updateItemQuantity(item);
        insertLog(null, itemId, NyanghwagwaInventoryLogType.PRODUCE, produceCount, item.getQuantity(), actorUsername, notes);
    }

    @Transactional
    public void sellSet(Long setId, int saleCount, String actorUsername, String notes) {
        if (saleCount <= 0) {
            throw new IllegalArgumentException("판매 수량은 1 이상이어야 합니다.");
        }
        NyanghwagwaItemSet set = requireSet(setId);
        List<NyanghwagwaItemSetEntry> entries = requireEntries(setId);

        int available = calculateSetStock(setId);
        if (available < saleCount) {
            throw new IllegalStateException("재고가 부족하여 판매할 수 없습니다. (보유: " + available + ")");
        }

        for (NyanghwagwaItemSetEntry entry : entries) {
            NyanghwagwaItem item = requireItem(entry.getItem_id());
            int delta = entry.getRequired_quantity() * saleCount;
            item.setQuantity(item.getQuantity() - delta);
            itemDAO.updateItemQuantity(item);
            insertLog(set.getSet_id(), item.getItem_id(), NyanghwagwaInventoryLogType.SALE, -delta,
                    item.getQuantity(), actorUsername, notes);
        }

        insertLog(set.getSet_id(), null, NyanghwagwaInventoryLogType.SALE, -saleCount, calculateSetStock(set.getSet_id()),
                actorUsername, notes);
    }

    public int calculateSetStock(Long setId) {
        List<NyanghwagwaItemSetEntry> entries = itemSetEntryDAO.selectEntriesBySetId(setId);
        if (CollectionUtils.isEmpty(entries)) {
            return 0;
        }
        Map<Long, NyanghwagwaItem> itemMap = toItemMap(itemDAO.selectAllItems());
        return entries.stream()
                .map(entry -> {
                    NyanghwagwaItem item = itemMap.get(entry.getItem_id());
                    if (item == null || item.getQuantity() == null) {
                        return 0;
                    }
                    Integer required = entry.getRequired_quantity();
                    if (required == null || required == 0) {
                        return Integer.MAX_VALUE;
                    }
                    return item.getQuantity() / required;
                })
                .min(Integer::compareTo)
                .orElse(0);
    }

    private void replaceSetEntries(Long setId, List<SetComponentInput> components) {
        itemSetEntryDAO.deleteEntriesBySetId(setId);

        if (CollectionUtils.isEmpty(components)) {
            return;
        }

        Set<Long> duplicatedCheck = new HashSet<>();
        for (SetComponentInput component : components) {
            if (component == null || component.itemId() == null) {
                throw new IllegalArgumentException("세트 구성 아이템을 선택해 주세요.");
            }
            if (!duplicatedCheck.add(component.itemId())) {
                throw new IllegalArgumentException("세트 구성에 같은 아이템이 중복되었습니다.");
            }
            if (component.requiredQuantity() == null || component.requiredQuantity() <= 0) {
                throw new IllegalArgumentException("세트 구성 아이템의 필요 수량은 1 이상이어야 합니다.");
            }
            requireItem(component.itemId());

            NyanghwagwaItemSetEntry entry = new NyanghwagwaItemSetEntry();
            entry.setSet_id(setId);
            entry.setItem_id(component.itemId());
            entry.setRequired_quantity(component.requiredQuantity());
            itemSetEntryDAO.insertEntry(entry);
        }
    }

    private Map<Long, NyanghwagwaItem> toItemMap(List<NyanghwagwaItem> items) {
        Map<Long, NyanghwagwaItem> map = new HashMap<>();
        if (items == null) {
            return map;
        }
        for (NyanghwagwaItem item : items) {
            if (item != null && item.getItem_id() != null) {
                map.put(item.getItem_id(), item);
            }
        }
        return map;
    }

    private NyanghwagwaSetStatusDto buildSetStatusDto(NyanghwagwaItemSet set,
                                                      List<NyanghwagwaItemSetEntry> entries,
                                                      Map<Long, NyanghwagwaItem> itemMap) {
        int stockQuantity = calculateAvailableSetCount(entries, itemMap);
        int totalItemCount = entries == null ? 0 : entries.size();

        return NyanghwagwaSetStatusDto.builder()
                .setId(set.getSet_id())
                .setName(set.getSet_name())
                .stockQuantity(stockQuantity)
                .totalItemCount(totalItemCount)
                .build();
    }

    private NyanghwagwaSetViewDto buildSetViewDto(NyanghwagwaItemSet set,
                                                  List<NyanghwagwaItemSetEntry> entries,
                                                  Map<Long, NyanghwagwaItem> itemMap) {
        List<NyanghwagwaSetComponentDto> components = CollectionUtils.isEmpty(entries)
                ? List.of()
                : entries.stream()
                .map(entry -> {
                    NyanghwagwaItem item = itemMap.get(entry.getItem_id());
                    return NyanghwagwaSetComponentDto.builder()
                            .itemId(entry.getItem_id())
                            .itemName(item != null ? item.getItem_name() : "알 수 없는 아이템")
                            .requiredQuantity(entry.getRequired_quantity())
                            .currentQuantity(item != null ? item.getQuantity() : 0)
                            .unit(item != null ? item.getUnit() : null)
                            .build();
                })
                .toList();

        boolean requireAlert = components.stream()
                .anyMatch(component -> component.getCurrentQuantity() < component.getRequiredQuantity());

        return NyanghwagwaSetViewDto.builder()
                .setId(set.getSet_id())
                .setName(set.getSet_name())
                .description(set.getDescription())
                .items(components)
                .requireAlert(requireAlert)
                .build();
    }

    private NyanghwagwaItemViewDto buildItemViewDto(NyanghwagwaItem item) {
        return NyanghwagwaItemViewDto.builder()
                .itemId(item.getItem_id())
                .itemName(item.getItem_name())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .description(item.getDescription())
                .build();
    }

    private int calculateAvailableSetCount(List<NyanghwagwaItemSetEntry> entries,
                                           Map<Long, NyanghwagwaItem> itemMap) {
        if (CollectionUtils.isEmpty(entries)) {
            return 0;
        }
        final Map<Long, NyanghwagwaItem> resolvedItemMap = itemMap != null ? itemMap : Map.of();
        return entries.stream()
                .map(entry -> {
                    NyanghwagwaItem item = resolvedItemMap.get(entry.getItem_id());
                    if (item == null || item.getQuantity() == null) {
                        return 0;
                    }
                    int required = entry.getRequired_quantity() == null || entry.getRequired_quantity() == 0
                            ? 0
                            : entry.getRequired_quantity();
                    if (required == 0) {
                        return Integer.MAX_VALUE;
                    }
                    return item.getQuantity() / required;
                })
                .min(Integer::compareTo)
                .orElse(0);
    }

    private NyanghwagwaItem requireItem(Long itemId) {
        NyanghwagwaItem item = itemDAO.selectItemById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("존재하지 않는 아이템입니다. (id=" + itemId + ")");
        }
        return item;
    }

    private NyanghwagwaItemSet requireSet(Long setId) {
        NyanghwagwaItemSet set = itemSetDAO.selectSetById(setId);
        if (set == null) {
            throw new IllegalArgumentException("존재하지 않는 세트입니다. (id=" + setId + ")");
        }
        return set;
    }

    private List<NyanghwagwaItemSetEntry> requireEntries(Long setId) {
        List<NyanghwagwaItemSetEntry> entries = itemSetEntryDAO.selectEntriesBySetId(setId);
        if (CollectionUtils.isEmpty(entries)) {
            throw new IllegalArgumentException("세트에 등록된 아이템 구성 정보가 없습니다. (id=" + setId + ")");
        }
        return entries;
    }

    private void insertLog(Long setId,
                           Long itemId,
                           NyanghwagwaInventoryLogType type,
                           int quantityChange,
                           int remaining,
                           String actor,
                           String notes) {
        NyanghwagwaInventoryLog log = new NyanghwagwaInventoryLog();
        log.setSet_id(setId);
        log.setItem_id(itemId);
        log.setLog_type(type);
        log.setQuantity_change(quantityChange);
        log.setRemaining_stock(remaining);
        log.setPerformed_at(LocalDateTime.now());
        log.setActor_username(actor);
        log.setNotes(notes);
        inventoryLogDAO.insertLog(log);
    }
}
