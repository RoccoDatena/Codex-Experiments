import { Injectable } from '@angular/core';

export type Direction = 'up' | 'down' | 'left' | 'right';

export interface Point {
  x: number;
  y: number;
}

export interface GameState {
  gridSize: number;
  snake: Point[];
  direction: Direction;
  nextDirection: Direction;
  food: Point;
  score: number;
  gameOver: boolean;
}

const DIR_VECTORS: Record<Direction, Point> = {
  up: { x: 0, y: -1 },
  down: { x: 0, y: 1 },
  left: { x: -1, y: 0 },
  right: { x: 1, y: 0 }
};

const OPPOSITES: Record<Direction, Direction> = {
  up: 'down',
  down: 'up',
  left: 'right',
  right: 'left'
};

@Injectable({ providedIn: 'root' })
export class SnakeGameService {
  createInitialState(gridSize = 20): GameState {
    const center = Math.floor(gridSize / 2);
    const snake = [
      { x: center, y: center },
      { x: center - 1, y: center },
      { x: center - 2, y: center }
    ];

    return {
      gridSize,
      snake,
      direction: 'right',
      nextDirection: 'right',
      food: { x: center + 3, y: center },
      score: 0,
      gameOver: false
    };
  }

  setDirection(state: GameState, direction: Direction): GameState {
    if (OPPOSITES[state.direction] === direction) {
      return state;
    }

    if (OPPOSITES[state.nextDirection] === direction) {
      return state;
    }

    return {
      ...state,
      nextDirection: direction
    };
  }

  placeFood(state: GameState, random: () => number = Math.random): Point {
    const freeCells: Point[] = [];

    for (let y = 0; y < state.gridSize; y += 1) {
      for (let x = 0; x < state.gridSize; x += 1) {
        if (!this.collidesWithSnake(state.snake, { x, y })) {
          freeCells.push({ x, y });
        }
      }
    }

    if (freeCells.length === 0) {
      return state.food;
    }

    const index = Math.floor(random() * freeCells.length);
    return freeCells[index];
  }

  tick(state: GameState, random: () => number = Math.random): GameState {
    if (state.gameOver) {
      return state;
    }

    const direction = state.nextDirection;
    const vector = DIR_VECTORS[direction];
    const head = state.snake[0];
    const nextHead = { x: head.x + vector.x, y: head.y + vector.y };

    if (
      nextHead.x < 0 ||
      nextHead.y < 0 ||
      nextHead.x >= state.gridSize ||
      nextHead.y >= state.gridSize
    ) {
      return { ...state, direction, gameOver: true };
    }

    const isGrowing = nextHead.x === state.food.x && nextHead.y === state.food.y;
    const snakeBody = isGrowing ? state.snake : state.snake.slice(0, -1);

    if (this.collidesWithSnake(snakeBody, nextHead)) {
      return { ...state, direction, gameOver: true };
    }

    const nextSnake = [nextHead, ...state.snake];
    if (!isGrowing) {
      nextSnake.pop();
    }

    let food = state.food;
    let score = state.score;

    if (isGrowing) {
      score += 1;
      food = this.placeFood({ ...state, snake: nextSnake }, random);
    }

    return {
      ...state,
      snake: nextSnake,
      direction,
      food,
      score,
      gameOver: false
    };
  }

  private collidesWithSnake(snake: Point[], point: Point): boolean {
    return snake.some((segment) => segment.x === point.x && segment.y === point.y);
  }
}