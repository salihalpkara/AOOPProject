package com.aoopproject.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.Function;

/**
 * Represents the game board as a 2D grid of {@link GameEntity} objects.
 * This class is generic and can hold any type that extends {@link GameEntity}.
 * It provides basic functionalities for accessing and modifying entities
 * on the grid.
 *
 * @param <T> The type of {@link GameEntity} this grid will hold.
 */
public class Grid<T extends GameEntity> {

    private final int rows;
    private final int columns;
    private final List<List<T>> gridData;

    /**
     * Constructs a new Grid with the specified number of rows and columns.
     * Initializes all cells to null.
     *
     * @param rows    The number of rows in the grid. Must be positive.
     * @param columns The number of columns in the grid. Must be positive.
     * @throws IllegalArgumentException if rows or columns are not positive.
     */
    public Grid(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive.");
        }
        this.rows = rows;
        this.columns = columns;
        this.gridData = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            List<T> row = new ArrayList<>(columns);
            for (int j = 0; j < columns; j++) {
                row.add(null);
            }
            this.gridData.add(row);
        }
    }

    /**
     * Constructs a new Grid and initializes each cell using the provided entity supplier.
     *
     * @param rows The number of rows in the grid.
     * @param columns The number of columns in the grid.
     * @param entitySupplier A supplier function that provides an instance of T for each cell.
     * This is called for each cell (rows * columns times).
     * @throws IllegalArgumentException if rows or columns are not positive.
     */
    public Grid(int rows, int columns, Supplier<T> entitySupplier) {
        this(rows, columns);
        if (entitySupplier == null) {
            throw new IllegalArgumentException("Entity supplier cannot be null.");
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                setEntity(r, c, entitySupplier.get());
            }
        }
    }


    /**
     * Gets the entity at the specified row and column.
     *
     * @param row    The row index (0-based).
     * @param column The column index (0-based).
     * @return The entity at the specified position, or null if the cell is empty
     * or coordinates are out of bounds.
     */
    public T getEntity(int row, int column) {
        if (isValidCoordinate(row, column)) {
            return this.gridData.get(row).get(column);
        }
        return null;
    }

    /**
     * Sets the entity at the specified row and column.
     *
     * @param row    The row index (0-based).
     * @param column The column index (0-based).
     * @param entity The entity to place in the cell. Can be null to represent an empty cell.
     * @throws IndexOutOfBoundsException if the coordinates are invalid.
     */
    public void setEntity(int row, int column, T entity) {
        if (!isValidCoordinate(row, column)) {
            throw new IndexOutOfBoundsException("Invalid grid coordinates: row=" + row + ", col=" + column);
        }
        this.gridData.get(row).set(column, entity);
    }

    /**
     * Gets the number of rows in the grid.
     *
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the grid.
     *
     * @return The number of columns.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Checks if the given coordinates are within the bounds of the grid.
     *
     * @param row    The row index.
     * @param column The column index.
     * @return {@code true} if the coordinates are valid, {@code false} otherwise.
     */
    public boolean isValidCoordinate(int row, int column) {
        return row >= 0 && row < this.rows && column >= 0 && column < this.columns;
    }

    /**
     * Fills the entire grid with entities provided by the supplier.
     * This will overwrite any existing entities.
     *
     * @param entitySupplier A function that supplies a new entity for each cell.
     */
    public void fill(Supplier<T> entitySupplier) {
        if (entitySupplier == null) {
            throw new IllegalArgumentException("Entity supplier cannot be null.");
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                setEntity(r, c, entitySupplier.get());
            }
        }
    }
    /**
     * Creates a deep copy of this grid.
     * Each entity in the grid is copied using the provided entity copier function.
     *
     * @param entityCopier A function that takes an entity of type T and returns a deep copy of it.
     * @return A new Grid instance containing deep copies of all entities.
     */
    public Grid<T> deepCopy(java.util.function.Function<T, T> entityCopier) {
        Grid<T> newGrid = new Grid<>(this.rows, this.columns);
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.columns; c++) {
                T originalEntity = this.getEntity(r, c);
                if (originalEntity != null) {
                    newGrid.setEntity(r, c, entityCopier.apply(originalEntity));
                } else {
                    newGrid.setEntity(r, c, null);
                }
            }
        }
        return newGrid;
    }
}