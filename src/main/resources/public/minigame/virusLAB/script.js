/**global variables */
/************************************************************/
/**colors */
const COLOR_MOUSE_DOWN = '#E5E7EA';
const COLOR_MOUSE_UP = '#FFFFFF';
const COLOR_MY_WALL = 'gray';

/** button */
const BUTTON_START_ID = "button-start";
const BUTTON_NEW_ID = "button-new";
const BUTTON_HINT_ID = "button-hint";
const BUTTON_SOLUTION_ID = "button-solution"

/** cell values */
const VIRUS_STRING = "π¦ ";      //  ui
const CELL_VALUE_NULL = -1;     // null
const CELL_VALUE_EMPTY = 0;
const CELL_VALUE_WALL = 1;
const CELL_VALUE_VIRUS = 2;

const ANSWER_LENGTH = 3;

/** game state */
const NOT_STARTED = 0
const PLAYING = 1
let GAME_STATE = NOT_STARTED;

const IMAGE_PATH = "./big-data.jpeg";

let N = 5;              // μ°κ΅¬μ€μ κ°λ‘ μΈλ‘ κΈΈμ΄
let labMap = [];        // μ°κ΅¬μ€μ ννν κ·Έλν
let wall = [];          // λ²½μ μμΉ (0~(N-1) μ μ)
let wallCoords = []     // 2μ°¨μ μ’νμμμ λ²½μ μμΉ
let answer = []         // μ λ΅(λ²½μ μ’ν μΈ κ°)
let answerIndex = []    // μ λ΅μ μ μ μΈλ±μ€λ‘ λ³ν
let playerAnswer = []   // μ¬μ©μκ° μλ ₯ν λ΅
let answerVirus = []    // μ λ΅μΌλμ λ°μ΄λ¬μ€κ° νΌμ§ μ’ν
let virus = [];         // λ°μ΄λ¬μ€μ μμΉ (0~(N-1) μ μ)
let maxSafety = 0       // μμ  μμ­ μ΅λκ°
let count = 0           // νμν κ²½μ°μ μ
let etime = 0           // μ λ΅ κ³μ°μ κ±Έλ¦° μκ°
/************************************************************/

/************************* UI *******************************/
/**
 * λ²νΌμ λλ μ λμ λ°μ
 * @param {DOM element} button
 */
function buttonMouseDown(button) {
    const mouseDownStyle = `background-color: ${COLOR_MOUSE_DOWN};
    border-left: 4px solid black;
    border-top: 4px solid black;
    border-right: 1px solid black;
    border-bottom: 1px solid black;`
    button.setAttribute("style", mouseDownStyle);
}

// TODO regex κ³΅λΆ
function selectCell(event) {
    const cell = event.target;
    const cellIndex = 1*cell.id.match(/\d+/)[0]
    const afterSelectedStyle = `background-color: ${COLOR_MY_WALL}; border: 1px solid black;`
    const beforeSelectedStyle = `background-color: white; border: 1px solid black;`

    let idx = -1;
    if ((idx = playerAnswer.indexOf(cellIndex)) > -1) {
        cell.setAttribute("style", beforeSelectedStyle)
        // playerAnswer = playerAnswer.filter((idx) => {1*idx!=cellIndex})
        playerAnswer.splice(idx, 1);
    } else if (playerAnswer.length >= ANSWER_LENGTH)
        alert(`λ²½μ ${ANSWER_LENGTH}κ°κΉμ§ μ€μΉν  μ μμ΅λλ€!`)
    // μ΄λ―Έ μ νν μμΈ κ²½μ° μ ν μ·¨μ
    // μμ§ μ ννμ§ μμλ μμΈ κ²½μ° μ ν
    else { 
        cell.setAttribute("style", afterSelectedStyle)
        playerAnswer.push(cellIndex)
    }
    console.log(playerAnswer)

    // μ λ΅ μ¬λΆ νλ¨
    let score = 0;
    if (playerAnswer.length == ANSWER_LENGTH) {
        for (const i of playerAnswer) {
            if (answerIndex.includes(i))
                score += 1;
        }
        if (score == ANSWER_LENGTH) 
            alert("μ λ΅μλλ€")
    }
}

/**
 * startλΆν° end-1κΉμ§μ μμμ μ μ 1κ°λ₯Ό λ°ννλ€.
 * @param {int} start include   
 * @param {int} end   exclude
 */
function getRandomInteger(start, end) {
    return Math.floor(Math.random() * (end-start) + start);
}

/**
 * λ²νΌ λ°μ μμ λ³΅κ·, λ²νΌ κΈ°λ₯ μ€ν
 * @param {DOM element} button
 */
function buttonMouseUp(button) {
    const mouseUpStyle = `background-color: ${COLOR_MOUSE_UP};
    border-left: 1px solid black;
    border-top: 1px solid black;
    border-right: 4px solid black;
    border-bottom: 4px solid black;`
    button.setAttribute("style", mouseUpStyle);

    const virusNum = getRandomInteger(2, 6)
    const wallNum = getRandomInteger(virusNum+2, virusNum+5)
    let start = 0;
    // TODO νλμ½λ©(5) μ¬μ©μλ‘λΆν° μλ ₯λ°λλ‘ μμ 
    // κ²μ μμ
    if (button.id == BUTTON_START_ID && GAME_STATE == NOT_STARTED) {
        start = new Date().getTime();
        do 
            initGameScreen(5, virusNum, wallNum);      // μ΄λ―Έ κ²μμ΄ μμλμλ€λ©΄ λ¬΄μ
        while (combination(0) == 0)
        etime = (new Date().getTime() - start);                   // μ λ΅μ κ΅¬νλλ° κ±Έλ¦° μκ° (ms)
    // μλ‘μ΄ κ²μ μμ
    } else if (button.id == BUTTON_NEW_ID && GAME_STATE == PLAYING) {  // μ κ²μ 
        clearSolutionInfo()
        start = new Date().getTime();
        do
            initGameScreen(5, virusNum, wallNum);      // μμ§ κ²μμ΄ μμλμ§ μμλ€λ©΄ λ¬΄μ
        while (combination(0) == 0)
        etime = (new Date().getTime() - start);                   // μ λ΅μ κ΅¬νλλ° κ±Έλ¦° μκ° (ms)
    // μ λ΅ λ³΄κΈ°
    } else if (button.id == BUTTON_SOLUTION_ID && GAME_STATE == PLAYING) {
        paintSolution();
    // ννΈ λ³΄κΈ°
    } else if (button.id == BUTTON_HINT_ID && GAME_STATE == PLAYING) {
        console.log(`μμ  μμ­ λμ΄κ° ${maxSafety} λλλ‘ λ²½μ μ€μΉνμΈμ`)
        alert(`μμ  μμ­ λμ΄κ° ${maxSafety} λλλ‘ λ²½μ μ€μΉνμΈμ`);
    }
    console.log(answerIndex)
}

/**
 * game-screen μ΄κΈ°ν
 * @param {int} n μ°κ΅¬μ€μ κ°λ‘ μΈλ‘ κΈΈμ΄
 * @param {int} v λ°μ΄λ¬μ€ κ°μ
 * @param {int} w λ²½ κ°μ
 */
function initGameScreen(n, v, w) {
    resetGame();
    GAME_STATE = PLAYING;
    N = n;
    setWallCoords(w)
    setVirusCoords(v)
    createLabCell(N);
    drawObjects();
    initGraph();
    combination(0);
}

/**
 * game-screenμ n*n κ°μ cell μμ±
 * @param {int} n μ°κ΅¬μ€ κ°λ‘ μΈλ‘ ν¬κΈ°
 */
function createLabCell(n) {
    const gameScreen = document.getElementById("game-screen");
    gameScreen.innerHTML = "";
    for (let i=0; i<(N*N); i++) {
        const cell = `<div class="labCell" id="labCell${i}"></div>`
        gameScreen.innerHTML += cell;
    }
}

/**
 * λ²½, λ°μ΄λ¬μ€λ₯Ό κ·Έλ¦°λ€.
 */
function drawObjects() {
    const wallStyle = "background-color: black;";
    const virusStyle = "font-size: 2.4em; padding-top: 10%;";
    const cellStyle = "border: 1px dotted black;"

    for (let i=0; i<(N*N); i++) {
        const query = `div#labCell${i}`
        const gameScreenLabCell = document.querySelector(query)
        if (wall.includes(i))
            gameScreenLabCell.setAttribute("style", wallStyle);
        else if (virus.includes(i)) {
            gameScreenLabCell.innerText = VIRUS_STRING;
            gameScreenLabCell.setAttribute("style", virusStyle + cellStyle);
        } else {
            gameScreenLabCell.setAttribute("style", cellStyle);
            gameScreenLabCell.classList.add("empty")
            gameScreenLabCell.addEventListener("click", selectCell)
        }
    }
}

/**
 * μ°κ΅¬μ€μ κ·Έλν ννλ‘ νν(μ μ­λ³μ labμ μ΄κΈ°ν)νλ€.
 */
function initGraph() {
    for (let x=0; x<N; x++) {
        let row = [];
        for (let y=0; y<N; y++)
            row.push(CELL_VALUE_NULL);
        labMap.push(row);
    }       
    for (let i=0; i<N*N; i++) {
        const x = Math.trunc(i/N);
        const y = i%N;
        if (wall.includes(i)) {
            labMap[x][y] = CELL_VALUE_WALL;
            // wallCoords.push([x,y]);
        }
        else if (virus.includes(i))
            labMap[x][y] = CELL_VALUE_VIRUS;
        else 
            labMap[x][y] = CELL_VALUE_EMPTY;
    }
}

/**
 * μ°κ΅¬μ€ ν μΉΈμ νν
 */
class LabMaplabMapCell {
    /**
     * cellμ x,y μ’ν μ΄κΈ°ν
     * @param {int} x 
     * @param {int} y 
     * @param {int} obj λΉ κ³΅κ°: 0, λ²½: 1, λ°μ΄λ¬μ€: 2 
     */
    constructor(x, y, obj) {
        this.x = x;
        this.y = y;
        this.obj = obj;
    }
}

/**
 * nκ°μ λ²½(λλ λ°μ΄λ¬μ€)μ μμΉλ₯Ό list<int> ννλ‘ λ°ννλ€.
 * @param {int} n μμ±ν  λ²½μ κ°μ
 */
function setWallCoords(n) {
    while (wall.length!=n) {
        num = Math.floor(Math.random() * (N*N));
        if (!wall.includes(num) && !virus.includes(num))
            wall.push(num)
    }
}

/**
 * nκ°μ λ°μ΄λ¬μ€μ μμΉλ₯Ό μ€μ 
 * @param {int} n μμ±ν  λ°μ΄λ¬μ€ κ°μ
 */
function setVirusCoords(n) {
    while (virus.length!=n) {
        num = Math.floor(Math.random() * (N*N));
        if (!wall.includes(num) && !virus.includes(num))
            virus.push(num)
    }
}

/**
 * κΈ°μ‘΄μ μ‘΄μ¬νλ κ²μ μ λ³΄λ₯Ό μ­μ .
 * λ°μ΄λ¬μ€, λ²½, μ°κ΅¬μ€ κ°λ‘ μΈλ‘ κΈΈμ΄ μ λ³΄
 */
function resetGame() {
    GAME_STATE = NOT_STARTED;
    labMap = [];
    wall = [];
    wallCoords = [];
    virus = [];
    N = 0;
    count = 0;
    etime = 0;
    maxSafety = 0;
    answerVirus = [];
    playerAnswer = []
    answerIndex = []
}

/**
 * μ λ΅ μΆλ ₯
 * answer λ°°μ΄μ μ€μΉν΄μΌ ν  λ²½μ μ’νκ° μ μ₯λμ΄ μλ€.
 */
function paintSolution() {
    // λ²½ νμ
    const answerStyle = `background-color: ${COLOR_MY_WALL}`;
    let targetCell = []
    for (const coord of answer)
        targetCell.push(coord[0]*N + coord[1])
    for (let i of targetCell) {
        const query = `div#labCell${i}`
        const gameScreenLabCell = document.querySelector(query)
            gameScreenLabCell.setAttribute("style", answerStyle);
    }
    // λ°μ΄λ¬μ€ νμ° κ²½λ‘ νμ
    const virusStyle = "font-size: 2.4em; padding-top: 10%; border: 1px dotted black;";
    targetCell = []
    for (const coord of answerVirus)
        targetCell.push(coord[0]*N + coord[1])
    for (let i of targetCell) {
        const query = `div#labCell${i}`
        const gameScreenLabCell = document.querySelector(query)
            gameScreenLabCell.innerText = VIRUS_STRING;
            gameScreenLabCell.setAttribute("style", virusStyle)
    }
    // solution-info μΆλ ₯
    // μκ°, κ²½μ°μ μ, μ΅λ μμ  μμ­ μΆλ ₯
    const timeInfo = `<div class="gcell-info" id="info-elapsed-time">κ±Έλ¦° μκ° <br><br> ${etime}ms</div>`;
    const caseInfo = `<div class="gcell-info" id="info-case-number">κ³μ°ν κ²½μ°μ μ <br><br> ${count}</div>`;
    const maxSafetyInfo = `<div class="gcell-info" id="info-maxSafety">μμ  μμ­ μ΅λκ° <br><br> ${maxSafety}</div>`;
    const solutionInfoContainer = document.getElementById("solution-info");
    solutionInfoContainer.innerHTML = '';
    solutionInfoContainer.innerHTML += caseInfo;
    solutionInfoContainer.innerHTML += timeInfo;
    solutionInfoContainer.innerHTML += maxSafetyInfo;
    // κ·Έλ¦Ό νμ
    const image = `<img id="info-ai-image" 
                        src=${IMAGE_PATH} 
                        width="100%"
                        alt="computer">`
    solutionInfoContainer.innerHTML += image;

}

function clearSolutionInfo() {
    const solutionInfo = document.getElementById("solution-info");
    solutionInfo.innerHTML = '';
}

/************************ μ λ΅ κ³μ° **************************/
const dx = [0, 1, 0, -1]
const dy = [1, 0, -1, 0]

function valid(graph, x, y) {
    return (x<N && x>-1 && y<N && y>-1 && graph[x][y]==0)
}

function spread(graph, sx, sy) {
    q=[]
    q.push([sx, sy])
    while (q.length != 0) {
        let [x, y] = q.shift()
        for (let i=0; i<4; i++) {
            let nextX = x + dx[i]
            let nextY = y + dy[i]
            if (!valid(graph, nextX, nextY))
                continue
            q.push([nextX, nextY])
            if (graph[x][y] == CELL_VALUE_VIRUS)
                graph[nextX][nextY] = CELL_VALUE_VIRUS
		}
	}
}

function countSafety(graph) {
    result = 0
    for (let x=0; x<N; x++) {
        for (let y=0; y<N; y++)
            if (graph[x][y] == CELL_VALUE_EMPTY)
                result += 1
	}
    return result
}

function findVIRUS(graph) {
    result = []
    for (let x=0; x<N; x++)
        for (let y=0; y<N; y++)
            if (graph[x][y] == CELL_VALUE_VIRUS)
                result.push([x, y])
    return result
}

function dcopy(dest, src) {
	for (let x=0; x<N; x++)
			for (let y=0; y<N; y++)
				dest[x][y] = src[x][y]
} 

/**
 * 
 * dfs λ°©μμΌλ‘ λ²½μ μΈ κ° μ€μΉνλ λͺ¨λ  κ²½μ°μ μ(ββCβ)μ λν΄ μμ  μμ­ κ³μ°
 * @param {int} index 
 * @returns 
 */
function combination(index) {
    // console.log(`combination executing... ${count}`)
    // λ²½ μΈ κ° μ€μΉ ν λ°μ΄λ¬μ€κ° νΌμ§ λ€ μμ  μμ­ λμ΄ μΈ‘μ 
    if (wallCoords.length == 3) {
        count += 1;
		// μ°κ΅¬μ€ μλ³Έ κ·Έλν νλ (deep copy)
        let data = []
		for (let x=0; x<N; x++) {
			let row = []
			for (let y=0; y<N; y++)
				row.push(labMap[x][y])
			data.push(row)
		}
        for (const w of wallCoords) {
            data[w[0]][w[1]] = CELL_VALUE_WALL;
		}
        // λ°μ΄λ¬μ€κ° νΌμ§λ κ²½μ° μλ?¬λ μ΄μ
        for (const v of findVIRUS(data)) {
            spread(data, v[0], v[1])
		}
        const safety = countSafety(data);
        // μ΅λκ° μλ°μ΄νΈ
        if (safety > maxSafety) {
            // λ°μ΄λ¬μ€κ° νΌμ§ κ²½λ‘ μ μ₯
            answerVirus = []
            for (let x=0; x<N; x++) {
                for (let y=0; y<N; y++) {
                    if (data[x][y] == CELL_VALUE_VIRUS)
                        answerVirus.push([x,y])
                }

            }
            // μμ  μμ­ κ° κ°±μ 
            maxSafety = safety;
            answer = [];
            answerIndex = [];
            for (const w of wallCoords) {
                answer.push(w);
                answerIndex.push(w[0]*N + w[1])
            }
        }
        return
	}
    else {
        for (let i=index; i<N*N; i++) {
            let x = Math.trunc(i/N)
			let y = i%N
            if (labMap[x][y] == CELL_VALUE_EMPTY) {
                wallCoords.push([x, y])
                combination(i+1)
                wallCoords.pop()
			}
            else
                continue    // λ²½μ μ€μΉν  μ μλ κ³΅κ°
		}
	}
}

/********************** for debugging ********************/
function printGameStat() {
    const stat = `μ°κ΅¬μ€ μ§λ: ${labMap} \n
                 μ€μΉν΄μΌνλ λ²½: ${answer} \n
                 μμ μμ­ μ΅λ ν¬κΈ°: ${maxSafety} \n
                 κ²μ μν: ${GAME_STATE} \n
                 νμν κ²½μ°μ μ: ${count}`

    console.log(stat)
}
/**
let N = 5;              // μ°κ΅¬μ€μ κ°λ‘ μΈλ‘ κΈΈμ΄
let labMap = [];        // μ°κ΅¬μ€μ ννν κ·Έλν
let wall = [];          // λ²½μ μμΉ (0~(N-1) μ μ)
let wallCoords = []     // 2μ°¨μ μ’νμμμ λ²½μ μμΉ
let answer = []         // μ λ΅(λ²½μ μ’ν μΈ κ°)
let virus = [];         // λ°μ΄λ¬μ€μ μμΉ (0~(N-1) μ μ)
let maxSafety = 0       // μμ  μμ­ μ΅λκ°
let count = 0           // νμν κ²½μ°μ μ
**/