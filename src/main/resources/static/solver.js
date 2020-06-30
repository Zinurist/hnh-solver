
var board = [ 2, 2,0,2, 0,0,0, 0,0,0, 1 ];
var divs = [];
var hare = null;
var hounds = [];
var solution = [-1,-1]

$(document).ready(init)

function harePos(){
    for(var i=0; i<11; i++) if(board[i] == 1) return i;
}
function houndsPos(){
    var pos = [];
    for(var i=0; i<11; i++) if(board[i] == 2) pos.push(i);
    return pos;
}

function init(){
    var boardDiv = document.getElementById("board");
    for(var i=0; i<11; i++){
        var x = i==0 ? 0 : Math.floor((i+2)/3);
        var y = (i==0||i==10) ? 1 : Math.floor((i+2)%3);
        var div = document.createElement("div");
        div.id = "board"+i;
        div.className = "boardTile";
        div.style.left = String(x*20)+"%";
        div.style.top = String(y*33.333)+"%";
        div.addEventListener('dragover', function() {allowDrop(event)});
        div.addEventListener('drop', function() {dropPiece(event)});
        div.boardPosition = i;
        boardDiv.appendChild(div);
        divs.push(div);
    }

    hare = document.createElement("img");
    hare.id = "hare";
    hare.className = "piece";
    hare.src = "imgs/hare.png";
    hare.draggable = true;
    hare.addEventListener('dragstart', function() {dragPiece(event)});

    for(var i=0; i<3; i++){
        hound = document.createElement("img");
        hound.id = "hound"+i;
        hound.className = "piece";
        hound.src = "imgs/hound.png";
        hound.draggable = true;
        hound.addEventListener('dragstart', function() {dragPiece(event)});
        hounds.push(hound);
    }

    drawBoard();
}

function drawBoard(){
    divs[harePos()].appendChild(hare);
    var houndsPosArr = houndsPos()
    for(var i=0; i<3; i++){
        divs[houndsPosArr[i]].appendChild(hounds[i]);
    }
}


function suggestMove(from, to){
    solution = [from, to];
    for(var i=0; i<11; i++){
        divs[i].className = (i==from||i==to) ? "boardTileSelected" : "boardTile";
    }
}

function dragPiece(ev){
    ev.dataTransfer.setData("text", ev.target.id);
}

function dropPiece(ev){
    var piece = document.getElementById(ev.dataTransfer.getData("text"));
    movePiece(piece.parentElement.boardPosition, ev.target.boardPosition);
}

function allowDrop(ev) {
    ev.preventDefault();
}

function movePiece(from, to){
    if(from == to || !(from>=0 && from<11 && to>=0 && to<11) ) return;
    var tmp = board[to];
    board[to] = board[from];
    board[from] = tmp;
    suggestMove(-1, -1);
    drawBoard();
}

function resetBoard(){
    board = [ 2, 2,0,2, 0,0,0, 0,0,0, 1 ];
    drawBoard();
}

async function fetchSolution(state){
    var response = await fetch('./solver', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(state)
    });
    if (response.status != 200) { console.log('Server error ' + response.status); return null; }
    return await response.json();
}


async function solve(player){
    console.log(board);
    document.getElementById('fetchStatus').innerHTML = "Status: Fetching solution";
    var state = {
        player: player,
        board: board
    };
    var solution = await fetchSolution(state);
    console.log(solution);
    if (solution === null) {
        document.getElementById('fetchStatus').innerHTML = "Status: Server error";
    } else if(solution.first == -1) {
             document.getElementById('fetchStatus').innerHTML = "Status: Game over";
     } else if(solution.first < -1) {
          document.getElementById('fetchStatus').innerHTML = "Status: Server error, invalid game state";
      } else {
        document.getElementById('fetchStatus').innerHTML = "Status: Displaying best move";
        suggestMove(solution.first, solution.second);
    }
}