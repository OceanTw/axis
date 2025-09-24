package dev.ocean.axis.utils.menu.impl;

import dev.ocean.axis.utils.menu.AbstractMenu;
import dev.ocean.axis.utils.menu.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class PaginatedMenu extends AbstractMenu {
    private int currentPage;
    private int maxPage;
    private List<Button> items;
    private final int[] itemSlots;
    private Button previousButton;
    private Button nextButton;
    private int previousSlot = -1;
    private int nextSlot = -1;

    public PaginatedMenu(String title, int size, List<Button> items) {
        super(title, size);
        this.items = new ArrayList<>(items);
        this.itemSlots = generateItemSlots();
        recalculateMaxPage();
        this.currentPage = 0;
        setupPage();
    }

    public PaginatedMenu(String title, int size, List<Button> items, int[] customItemSlots) {
        super(title, size);
        this.items = new ArrayList<>(items);
        this.itemSlots = customItemSlots;
        recalculateMaxPage();
        this.currentPage = 0;
        setupPage();
    }

    private int[] generateItemSlots() {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private void recalculateMaxPage() {
        this.maxPage = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / itemSlots.length) - 1;
    }

    public void nextPage() {
        if (currentPage < maxPage) {
            currentPage++;
            setupPage();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            setupPage();
        }
    }

    public void setPage(int page) {
        if (page >= 0 && page <= maxPage) {
            currentPage = page;
            setupPage();
        }
    }

    public void setPaginationButtons(Button previous, Button next, int previousSlot, int nextSlot) {
        this.previousButton = previous;
        this.nextButton = next;
        this.previousSlot = previousSlot;
        this.nextSlot = nextSlot;
        setupPage();
    }

    private void setupPage() {
        for (int slot : itemSlots) {
            removeButton(slot);
        }

        int startIndex = currentPage * itemSlots.length;
        for (int i = 0; i < itemSlots.length && startIndex + i < items.size(); i++) {
            setButton(itemSlots[i], items.get(startIndex + i));
        }

        if (previousButton != null && previousSlot != -1) {
            if (currentPage > 0) {
                setButton(previousSlot, previousButton);
            } else {
                removeButton(previousSlot);
            }
        }

        if (nextButton != null && nextSlot != -1) {
            if (currentPage < maxPage) {
                setButton(nextSlot, nextButton);
            } else {
                removeButton(nextSlot);
            }
        }
    }

    public void addItem(Button button) {
        items.add(button);
        recalculateMaxPage();
        setupPage();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalculateMaxPage();
            setupPage();
        }
    }

    public void setItems(List<Button> newItems) {
        this.items = new ArrayList<>(newItems);
        recalculateMaxPage();
        if (currentPage > maxPage) currentPage = maxPage;
        setupPage();
    }

    public void refresh() {
        setupPage();
    }

    public List<Button> getItems() {
        return new ArrayList<>(items);
    }
}