import { SnakeGameService, GameState } from './snake-game.service';

describe('SnakeGameService', () => {
  let service: SnakeGameService;

  beforeEach(() => {
    service = new SnakeGameService();
  });

  it('moves snake one cell on tick', () => {
    const state = service.createInitialState(10);
    const next = service.tick(state);

    expect(next.snake[0]).toEqual({ x: state.snake[0].x + 1, y: state.snake[0].y });
    expect(next.snake.length).toBe(state.snake.length);
  });

  it('prevents immediate opposite direction', () => {
    const state = service.createInitialState(10);
    const next = service.setDirection(state, 'left');

    expect(next.nextDirection).toBe('right');
  });

  it('grows and increments score when food is eaten', () => {
    const state: GameState = {
      ...service.createInitialState(10),
      snake: [
        { x: 4, y: 4 },
        { x: 3, y: 4 },
        { x: 2, y: 4 }
      ],
      direction: 'right',
      nextDirection: 'right',
      food: { x: 5, y: 4 },
      score: 0
    };

    const next = service.tick(state, () => 0);

    expect(next.score).toBe(1);
    expect(next.snake.length).toBe(4);
    expect(next.snake[0]).toEqual({ x: 5, y: 4 });
  });

  it('sets game over on wall collision', () => {
    const state: GameState = {
      ...service.createInitialState(6),
      snake: [
        { x: 5, y: 2 },
        { x: 4, y: 2 },
        { x: 3, y: 2 }
      ],
      direction: 'right',
      nextDirection: 'right'
    };

    const next = service.tick(state);

    expect(next.gameOver).toBeTrue();
  });

  it('places food on a free cell only', () => {
    const state: GameState = {
      ...service.createInitialState(3),
      snake: [
        { x: 0, y: 0 },
        { x: 1, y: 0 },
        { x: 2, y: 0 },
        { x: 0, y: 1 },
        { x: 1, y: 1 },
        { x: 2, y: 1 },
        { x: 0, y: 2 },
        { x: 1, y: 2 }
      ],
      food: { x: 2, y: 2 }
    };

    const food = service.placeFood(state, () => 0);

    expect(food).toEqual({ x: 2, y: 2 });
  });
});