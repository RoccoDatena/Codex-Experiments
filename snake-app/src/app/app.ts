import { Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Direction, GameState, SnakeGameService } from './snake-game.service';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  @ViewChild('board', { static: true }) boardRef!: ElementRef<HTMLCanvasElement>;

  readonly gridSize = 20;
  readonly tickMs = 140;

  state: GameState;
  paused = false;
  bestScore = 0;

  private readonly bestScoreKey = 'snake-best-score';
  private timerId: ReturnType<typeof setInterval> | null = null;
  private ctx: CanvasRenderingContext2D | null = null;

  constructor(private readonly game: SnakeGameService) {
    this.state = this.game.createInitialState(this.gridSize);
    this.bestScore = this.readBestScore();
  }

  ngOnInit(): void {
    this.ctx = this.boardRef.nativeElement.getContext('2d');
    this.render();
    this.timerId = setInterval(() => this.update(), this.tickMs);
  }

  ngOnDestroy(): void {
    if (this.timerId !== null) {
      clearInterval(this.timerId);
    }
  }

  get statusText(): string {
    if (this.state.gameOver) {
      return 'Game Over';
    }

    return this.paused ? 'Paused' : 'Running';
  }

  restart(): void {
    this.state = this.game.createInitialState(this.gridSize);
    this.paused = false;
    this.render();
  }

  togglePause(): void {
    if (this.state.gameOver) {
      return;
    }

    this.paused = !this.paused;
  }

  setDirection(direction: Direction): void {
    if (this.paused || this.state.gameOver) {
      return;
    }

    this.state = this.game.setDirection(this.state, direction);
  }

  @HostListener('window:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (event.code === 'Space') {
      event.preventDefault();
      this.togglePause();
      return;
    }

    const keyMap: Record<string, Direction> = {
      ArrowUp: 'up',
      ArrowDown: 'down',
      ArrowLeft: 'left',
      ArrowRight: 'right',
      w: 'up',
      W: 'up',
      s: 'down',
      S: 'down',
      a: 'left',
      A: 'left',
      d: 'right',
      D: 'right'
    };

    const direction = keyMap[event.key];
    if (direction !== undefined) {
      event.preventDefault();
      this.setDirection(direction);
    }
  }

  private update(): void {
    if (this.paused || this.state.gameOver) {
      return;
    }

    this.state = this.game.tick(this.state);
    this.persistBestScoreIfNeeded();
    this.render();
  }

  private readBestScore(): number {
    try {
      const raw = localStorage.getItem(this.bestScoreKey);
      const value = raw === null ? 0 : Number(raw);
      return Number.isFinite(value) && value >= 0 ? value : 0;
    } catch {
      return 0;
    }
  }

  private persistBestScoreIfNeeded(): void {
    if (this.state.score <= this.bestScore) {
      return;
    }

    this.bestScore = this.state.score;
    try {
      localStorage.setItem(this.bestScoreKey, String(this.bestScore));
    } catch {
      // Ignore storage errors and keep in-memory value.
    }
  }

  private render(): void {
    if (this.ctx === null) {
      return;
    }

    const canvas = this.boardRef.nativeElement;
    const cellSize = canvas.width / this.gridSize;

    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    this.drawField(canvas);
    this.drawSoccerBallFood(cellSize);

    this.state.snake.forEach((segment, index) => {
      this.ctx!.fillStyle = index === 0 ? '#0f1b2d' : '#0f4c81';
      this.ctx!.fillRect(segment.x * cellSize + 1, segment.y * cellSize + 1, cellSize - 2, cellSize - 2);
    });
  }

  private drawSoccerBallFood(cellSize: number): void {
    if (this.ctx === null) {
      return;
    }

    const centerX = this.state.food.x * cellSize + cellSize / 2;
    const centerY = this.state.food.y * cellSize + cellSize / 2;
    const radius = Math.max(4, cellSize / 2 - 2);

    this.ctx.beginPath();
    this.ctx.fillStyle = '#ffffff';
    this.ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
    this.ctx.fill();

    this.ctx.strokeStyle = '#111111';
    this.ctx.lineWidth = 1.4;
    this.ctx.stroke();

    this.ctx.fillStyle = '#111111';
    this.ctx.beginPath();
    this.ctx.arc(centerX, centerY, radius * 0.28, 0, Math.PI * 2);
    this.ctx.fill();

    const spotRadius = radius * 0.14;
    const spotOffset = radius * 0.55;
    const spots = [
      { x: centerX, y: centerY - spotOffset },
      { x: centerX + spotOffset * 0.85, y: centerY - spotOffset * 0.25 },
      { x: centerX + spotOffset * 0.55, y: centerY + spotOffset * 0.75 },
      { x: centerX - spotOffset * 0.55, y: centerY + spotOffset * 0.75 },
      { x: centerX - spotOffset * 0.85, y: centerY - spotOffset * 0.25 }
    ];

    spots.forEach((spot) => {
      this.ctx!.beginPath();
      this.ctx!.arc(spot.x, spot.y, spotRadius, 0, Math.PI * 2);
      this.ctx!.fill();
    });
  }

  private drawField(canvas: HTMLCanvasElement): void {
    if (this.ctx === null) {
      return;
    }

    this.ctx.fillStyle = '#9fce71';
    this.ctx.fillRect(0, 0, canvas.width, canvas.height);

    this.ctx.fillStyle = 'rgba(124, 177, 73, 0.28)';
    for (let stripe = 0; stripe < 5; stripe += 1) {
      this.ctx.fillRect(0, stripe * 80, canvas.width, 40);
    }

    this.ctx.strokeStyle = 'rgba(245, 255, 230, 0.65)';
    this.ctx.lineWidth = 2;
    this.ctx.beginPath();
    this.ctx.moveTo(canvas.width / 2, 0);
    this.ctx.lineTo(canvas.width / 2, canvas.height);
    this.ctx.stroke();

    this.ctx.beginPath();
    this.ctx.arc(canvas.width / 2, canvas.height / 2, 38, 0, Math.PI * 2);
    this.ctx.stroke();
  }
}