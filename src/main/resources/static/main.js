//className 변수
const searchInput = document.querySelector('.search-input');
const searchResult = document.querySelector('.search-result');
const acceptArea = document.querySelector('.accept-area');
const matchingArea = document.querySelector('.matchingArea');
const quizBox = document.querySelector('.quizBox'); // 퀴즈 출력 공간
const timeBox = document.querySelector('.timeBox'); // 남은 시간초 출력 공간
const canvas = document.querySelector('.myCanvas'); // 캔버스
const clearBtn = document.querySelector('.clearBtn'); // clearBtn
const rollBackBtn = document.querySelector('.rollBackBtn'); // rollBackBtn
const ctx = canvas.getContext("2d");
const answerBox = document.querySelector('.answerBox');
const sendBtn = document.querySelector('.sendBtn');
const blackBtn = document.querySelector('.blackBtn');
const redBtn = document.querySelector('.redBtn');
const blueBtn = document.querySelector('.blueBtn');
const userNameBoxList = [document.querySelector('.user1_name'), document.querySelector('.user2_name'), document.querySelector('.user3_name'), document.querySelector('.user4_name')];
const userAnswerBoxList = [document.querySelector('.user1_answer'), document.querySelector('.user2_answer'), document.querySelector('.user3_answer'), document.querySelector('.user4_answer')];
const userAreaBoxList = [document.querySelector('.user1_area'), document.querySelector('.user2_area'), document.querySelector('.user3_area'), document.querySelector('.user4_area')]
const inGameMenu = document.querySelector('.inGameMenu');
const gameArea = document.querySelector('.gameArea');
//id 변수
const memberListTable = document.getElementById('memberListTable');
const chattingArea = document.getElementById('chattingArea');
const memberNameArea = document.getElementById('memberNameArea');
const chatContentArea = document.getElementById('chatContentArea');
const message = document.getElementById('message');
const myIdArea = document.getElementById('myIdArea');
const matchingStartBtn = document.getElementById('matchingStartBtn');
const matchingCancleBtn = document.getElementById('matchingCancleBtn');
let myId = '';
let chattingAreaMemberName = '';
let chattingAreaIsTrue = false;
let chattingData = [];
let memberList = [];
let friendList = [];
let myTurn;
let cycle = 0;
let isCorrect = false;
let time = 0;
let color = 'black'; //색깔 변수 기본 black
let turn = 0;
let xy = [];
let ram = [];
let superRam = [];
let quiz = '';
let answer = '';
let userList = [];
let lastXY={
    lastX: 0,
    lastY: 0
};
let mouseState = {
    mouse : false,
    isout : false
};
var ws;
gameArea.style.display = 'none';

//GET 요청 함수
async function getRequest(url = '') {
    try {
        const response = await fetch(url, { method: 'GET' });

        if (!response.ok) {
            throw new Error(
                'Network response was not ok ' + response.statusText
            );
        }

        // JSON 형식으로 변환된 데이터를 가져옴.
        const responseData = await response.json();

        return responseData;
    } catch (error) {
        console.error('Fetch operation failed:', error);
    }
}
//유저 검색 함수
async function searchMember() {
    if(searchMemberValue.value != myId){
        const url1 = `/member/isAlreadyFriend?id=${searchMemberValue.value}&myId=${myId}`;
        let isAlreadyFriend = await getRequest(url1);

        // result == 0 친구 아님, result == 1 이미 친구
        if(isAlreadyFriend.result == 0){
            const url = `/member/searchMember?id=${searchMemberValue.value}`;
            let result = await getRequest(url);
            if (result.id != null) {
                searchResult.innerHTML = `
                    <div class="result-name">${result.id}</div>
                    <button class="plus-btn" onclick="addFriendRequest('${result.id}')">+</button>
                `;
            }
        }
        if(isAlreadyFriend.result == 1){
            const url = `/member/searchMember?id=${searchMemberValue.value}`;
            let result = await getRequest(url);
            if (result.id != null) {
                searchResult.innerHTML = `
                    <div class="result-name">${result.id}</div>
                    <div style="color: red; font-size:10px;">이미친구입니다.</div>
                `;
            }
        }
    }
}
//생성자
function RequestParam(request='', receiver = '', data = '') {
    this.request = request;
    this.receiver = receiver;
    this.data = data;
}
function GameRequestParam(request='', x=0, y=0, lastX=0, lastY=0, color=''){
    this.request = request;
    this.coordinate = [x, y, lastX, lastY];
    this.color = color;
}
//로그인 후 main 페이지 로드 시 소캣 연걸
window.onload = wsOpen;
//websocket open
function wsOpen() {
    ws = new WebSocket('ws://' + location.host + '/start');
    wsEvt();
}
//소캣 이벤트
function wsEvt() {
    //소캣 오픈 이벤트
    ws.onopen = function (data) {
        //
    };
    //소캣 메시지 이벤트
    ws.onmessage = function (data) {
        let msg = JSON.parse(data.data);
        receiveMessageHandler(msg);
    };
}
//메시지 처리 핸들러 함수
function receiveMessageHandler(msg) {
    //request -> duplicateLogin
    if(msg.request == 'duplicateLogin'){
        alert('다른 곳 에서 로그인 시도. 로그아웃 됩니다.');
        location.href = '/';
    }
    //request -> addFriendResponse
    if(msg.request == 'addFriendResponse'){
        if(msg.data == 'true'){
            //
        }
    }
    //request -> leaveOtherMember
    if(msg.request == 'leaveOtherMember'){
        alert('다른 유저가 게임을 떠났습니다.');
        location.href = '/main';
    }
    //request -> answer
    if(msg.request == 'answer'){
        for(let i = 0; i < userList.length; i++){
            if(userList[i] == msg.sender){
                userAnswerBoxList[i].innerHTML = `${msg.data}`;
            }
        }
        if(msg.data == answer){
            quizBox.innerHTML = `${msg.sender}님 정답!!! -> ${answer}`;
            time = 5;
        }

    }
    //request -> gameOver
    if(msg.request == 'gameOver'){
        quizBox.innerHTML = `잠시 후 게임이 종료됩니다.`;
        time = 5;
        timeCalculation(time, timeBox)
        function timeCalculation(time, box){
            box.innerHTML = time;
            setTimeout(function() {
                if(time > 1){
                    time--;
                    timeCalculation(time, box);
                }
                else{
                    matchingArea.style.display = 'block';
                    gameArea.style.display = 'none';
                }
            }, 1000);
        }
    }
    //request -> timeCount
    if(msg.request == 'timeCount'){ timeBox.innerHTML = `${msg.data}`; }
    //request -> gameStart, nextTurn
    if(msg.request == 'gameStart' || msg.request == 'nextTurn'){
        quiz = msg.quiz;
        answer = msg.answer;
        if(msg.yourTurn == myTurn){
            sendBtn.removeEventListener('click', sendAnswer);
            if(myTurn == 2){ cycle++; }
            addListener();
            time = 60;
            quizBox.innerHTML = `${msg.answer}`;
            timeBox.innerHTML = `${time}`;
            const requestParam = new RequestParam('timeCount', '', time);
            timeCalculation1();
            function timeCalculation1(){
                setTimeout(function() {
                    if(time > 1){
                        time--;
                        requestParam.data = time;
                        send(requestParam);
                        timeCalculation1();
                    }else{
                        if(cycle == 2 && myTurn == 2){
                            requestParam.request = 'gameOver';
                            send(requestParam);
                            cycle = 0;
                        }else{
                            removeListener();
                            requestParam.request = 'nextTurn';
                            requestParam.data = myTurn;
                            send(requestParam);
                            clear();
                            clear_no_send();
                        }
                    }
                }, 1000);
            }
        }else{
            sendBtn.addEventListener('click', sendAnswer);
            quizBox.innerHTML = `${msg.quiz}`;
        }
    }
    //request -> rollBack
    if(msg.request == 'rollBack'){
        rollBack_no_send();
    }
    //request -> push
    if(msg.request == 'push'){
        superRam.push(ram);
        ram = [];
    }
    //request -> clear
    if(msg.request == 'clear'){ clear_no_send(); }
    //request -> sendCoordinate
    if(msg.request == 'sendCoordinate'){
        draw(msg.coordinate[0], msg.coordinate[1], msg.coordinate[2], msg.coordinate[3], msg.color);
        ram.push({ x: msg.coordinate[0], y: msg.coordinate[1], lastX: msg.coordinate[2], lastY: msg.coordinate[3], color: msg.color });
        console.log([1,2,3,4,'black']);

    }
    //request -> matchingStartDrowGame
    if(msg.request == 'matchingStartDrowGame' && msg.response == 'success'){
        myTurn = msg.yourTurn;
        userList = msg.roomUsers;

        for(let i = 0; i<4; i++){
            if(i<userList.length){
                userNameBoxList[i].innerHTML = `${userList[i]}`;
                userAnswerBoxList[i].style.display = 'block';
            }else{
                userAreaBoxList[i].style.display = 'none';
            }
        }


        matchingArea.style.display = 'none';
        gameArea.style.display = 'block';
        quizBox.innerHTML = `잠시 후 게임이 시작됩니다.`;

        time = 3;
        timeCalculation(time, timeBox)
        function timeCalculation(time, box){
            box.innerHTML = time;
            setTimeout(function() {
                if(time > 1){
                    time--;
                    timeCalculation(time, box);
                }
                else{
                    const requestParam = new RequestParam('gameStart');
                    send(requestParam);
                }
            }, 1000);
        }
    }
    //request -> addFriendRequest
    if(msg.request == 'addFriendRequest'){ addFriendProc(msg); }
    //request -> sendMessage
    if (msg.request == 'sendMessage') { sendMessageProc(msg); }
    //request -> array
    if (Array.isArray(msg)) {                       //배열을 받았을 경우
        //채팅 데이터인지 확인
        if(msg[0].request == 'chattingData'){
            for(let i = 0; i<friendList.length; i++){
                let data = [];
                for(let j = 0; j<msg.length; j++){
                    if((msg[j].sender == friendList[i].member_id && msg[j].receiver == friendList[i].friend_id) ||
                        (msg[j].sender == friendList[i].friend_id && msg[j].receiver == friendList[i].member_id)){
                        data.push(msg[j]);
                    }
                }
                chattingData.push(data);
            }
        }
        if(msg[0].request == 'friendList'){         //친구 목록
            friendList = msg;
            document.querySelector('.user-menu').innerHTML = '';
            inputFriendList(friendList);
        }
    }
    //myId
    if (msg.myId != undefined) {
        myIdArea.innerHTML = `${msg.myId}`;
        myId = msg.myId;
    }
    //다른 유저 로그인 status 수정
    if(msg.loginMember != undefined){
        for(let i = 0; i<friendList.length; i++){
            if(msg.loginMember == friendList[i].friend_id){
                friendList[i].status = 'online';
                document.querySelector('.user-menu').innerHTML = '';
                inputFriendList(friendList)
            }
        }
    }
    //다른 유저 로그아웃 status 수정
    if (msg.logOutMember != undefined) {
        for(let i = 0; i<friendList.length; i++){
            if(msg.logOutMember == friendList[i].friend_id){
                friendList[i].status = 'offline';
                document.querySelector('.user-menu').innerHTML = '';
                inputFriendList(friendList);
            }
        }
    }
}
//친구추가 요청 처리 함수
function addFriendProc(msg){
    acceptArea.innerHTML = `
        <div class="accept-msg-area">
            <p class="accept-msg">${msg.sender} 님이</p>
            <p class="accept-msg">친구추가 요청을 보냈습니다.</p>
            <div class="accept-check">
                <div class="yes" onclick="addFriendResponse(true, '${msg.sender}')">yes</div>
                <div class="no" onclick="addFriendResponse(false, '${msg.sender}')">no</div>
            </div>
        </div>
    `;
    acceptArea.style.display = 'block';
}
// 채팅 메시지 처리 함수///////////////////////////////////////////
function sendMessageProc(msg){
    //내가 보낸 메시지
    if(msg.sender == myId && msg.receiver == member){
        chatContentArea.innerHTML += `
            <div class="chatBallonArea">
                <p class="chat-ballon1">${msg.content}</p>
            </div>
        `;
    }
    //상대가 보낸 메시지
    if(msg.sender == member && msg.receiver == myId){
        chatContentArea.innerHTML += `
            <div class="chatBallonArea">
                <p class="chat-ballon2">${msg.content}</p>
            </div>
        `;
    }
    for(let i = 0; i<friendList.length; i++){
        if((msg.sender == friendList[i].member_id && msg.receiver == friendList[i].friend_id) ||
            (msg.sender == friendList[i].friend_id && msg.receiver == friendList[i].member_id)){
            chattingData[i].push(msg);
        }
    }
    chatContentArea.scrollTop = chatContentArea.scrollHeight;       //스크롤 위치 조정
}
// 매칭 함수
function matching(request) {
    if (request == 'drowGameStart') {
        const requestParam = new RequestParam('matchingStartDrowGame');
        send(requestParam);
        matchingStartBtn.style.display = 'none';
        matchingCancleBtn.style.display = 'block';
    }

    if (request == 'drowGameCancle') {
        const requestParam = new RequestParam('matchingCancleDrowGame');
        send(requestParam);

        matchingStartBtn.style.display = 'block';
        matchingCancleBtn.style.display = 'none';
    }
}
// 친구 추가 요청 전송
function addFriendRequest(id) {
    const requestParam = new RequestParam('addFriendRequest', id);
    send(requestParam);
}
// 친구 추가 요청 응답 전송 -> 수락 및 거절 이후 화면 처리 필요
function addFriendResponse(response, receiver){
    const requestParam = new RequestParam('addFriendResponse', receiver, response);
    send(requestParam);
    acceptArea.style.display='none';
}
//친구목록 출력
function inputFriendList(friendList){
    for(let i = 0; i < friendList.length; i++){
        if(friendList[i].status == 'online'){
            setMemberStateOnline(friendList[i].friend_id);
        }

        if(friendList[i].status == 'offline'){
            setMemberStateOffline(friendList[i].friend_id);
        }
    }
}
//유저 온라인 상태 set
function setMemberStateOnline(id){
    document.querySelector('.user-menu').insertAdjacentHTML(
        'beforeend',
        `
            <li class="user-list" onclick="chat('${id}')">
                <div class="user-name">${id}</div>
                <div class="user-online"></div>
            </li>
        `
    );
}
//유저 오프라인 상태 set
function setMemberStateOffline(id){
    document.querySelector('.user-menu').insertAdjacentHTML(
        'beforeend',
        `
            <li class="user-list" onclick="chat('${id}')">
                <div class="user-name">${id}</div>
                <div class="user-offline"></div>
            </li>
        `
    );
}
//현재 열려있는 채팅창의 member 를 저장
let member;
function getMember() { return member; }
function setMember(m) { member = m; }
function chat(member) {
    setMember(member);
    memberNameArea.innerHTML = member;

    //채팅 공간 토글 처리
    if (chattingAreaMemberName != member) {
        chattingAreaMemberName = member;
    } else {
        chattingAreaIsTrue = !chattingAreaIsTrue;
    }
    //채팅 공간이 열려있을 때
    if (chattingAreaIsTrue) {
        chattingArea.style.display = 'none';
    } else {
        chatContentArea.innerHTML = '';
        inputAllChattingData(member);
        chattingArea.style.display = 'block';
        chatContentArea.scrollTop = chatContentArea.scrollHeight;
    }
}
//채팅공간에 채팅 데이터 입력
function inputAllChattingData(member) {
    chatContentArea.innerHTML = '';
    for(let i = 0; i<friendList.length; i++){
        if(member == friendList[i].friend_id){
            for(let j = 0; j<chattingData[i].length; j++){
                if (chattingData[i][j].sender == myId) {
                    chatContentArea.innerHTML += `
                        <div class="chatBallonArea">
                            <p class="chat-ballon1">${chattingData[i][j].content}</p>
                        </div>
                    `;
                } else {
                    chatContentArea.innerHTML += `
                        <div class="chatBallonArea">
                            <p class="chat-ballon2">${chattingData[i][j].content}</p>
                        </div>
                    `;
                }
            }
        }
    }
}
//메시지 set
function setMessage() {
    const requestParam = new RequestParam('sendMessage'
        ,memberNameArea.innerHTML
        ,message.value
    );
    send(requestParam);
    message.value = '';
}
//메시지 전송
function send(requestParam) { ws.send(JSON.stringify(requestParam)); }
document
  .querySelector('#chattingArea')
  .addEventListener('click', function (e) {
    if (e.target == document.querySelector('.down')) {
      document.querySelector('.chattingBig').style.display = 'none';
      document.querySelector('#chattingArea').style.padding = '0';
    } else if (e.target == document.querySelector('.close')) {
      document.querySelector('#chattingArea').style.display = 'none';
    } else if (e.target == document.querySelector('.chatUserName')) {
      document.querySelector('.chatUserName').innerHTML = getMember();
      document.querySelector('.chattingBig').style.display = 'block';
      document.querySelector('#chattingArea').style.padding = '10px 20px';
    }
  });
//=============//
//===모달창====//
//============//
document.querySelector('.add-friends').addEventListener('click', () => {
  document.querySelector('.black-area').style.display = 'block';
});
document.querySelector('.black-area').addEventListener('click', (e) => {
  if (e.target == document.querySelector('.black-close')) {
    document.querySelector('.black-area').style.display = 'none';
  }
});
//===============//
//===회원정보====//
//===============//
let show = false;
async function getMemberInfo(){
    const url = `/member/getMemberInfo`;
    let result = await getRequest(url);
    document.querySelector('.profile-black').style.display = 'block';
    document.querySelector('.profile-white').style.display = 'block';
    document.querySelector('.profile-white').innerHTML = `
        <h2>회원정보</h2>
        <ul>
            <li>
                <p>아이디 : ${result.id}</p>
                <input type="text" placeholder="아이디" />
            </li>
            <li>
                <p>비밀번호 : ${result.password}</p>
                <input type="text" placeholder="비밀번호" />
            </li>
            <li>
                <p>이름 : ${result.name}</p>
                <input type="text" placeholder="이름" />
            </li>
            <li>
                <p>이메일 : ${result.email}</p>
                <input type="email" placeholder="email@naver.com" />
            </li>
        </ul>
        <div class="profile-btn">
            <button>수정</button>
            <button>닫기</button>
        </div>
    `;
}

document
    .querySelector('.profile-black')
    .addEventListener('click', (e) => {
        if (e.target == document.querySelectorAll('.profile-btn button')[1]) {
            document.querySelector('.profile-black').style.display = 'none';
        }
        if (e.target == document.querySelectorAll('.profile-btn button')[0]) {
            show = !show;
            let input = document.querySelectorAll('.profile-white input');
            if (show == true) {
                input.forEach((a, i) => {
                    input[i].style.display = 'block';
                });
        } else {
            input.forEach((a, i) => {
                input[i].style.display = 'none';
            });
        }
    }
});


//===================================== 게임 자바스크립트 모음 =====================================//

//이벤트 리스너 추가 함수
function addListener(){
    blackBtn.addEventListener('click', blackBtnClickHandler);
    blueBtn.addEventListener('click', blueBtnClickHandler);
    redBtn.addEventListener('click', redBtnClickHandler);
    clearBtn.addEventListener('click', clear);
    rollBackBtn.addEventListener('click', rollBack);
    canvas.addEventListener('mousedown', canvasMouseDownHandler);
}
function removeListener(){
    blackBtn.removeEventListener('click', blackBtnClickHandler);
    blueBtn.removeEventListener('click', blueBtnClickHandler);
    redBtn.removeEventListener('click', redBtnClickHandler);
    clearBtn.removeEventListener('click', clear);
    rollBackBtn.removeEventListener('click', rollBack);
    canvas.removeEventListener('mousedown', canvasMouseDownHandler);
}
function canvasMouseDownHandler(event) {
    mouseState.mouse = true;        // 마우스 클릭 상태 true
    mouseState.isout = false;       // 마우스가 캔버스 위에 위치
    mouseHandler(event);
};
function mouseHandler(event){
    if(mouseState.isout==true){
        ram=[];     //ram 배열 초기화
    }else{
        //마우스를 누른 상태
        if(mouseState.mouse == true){
            ram = [];
            lastXY.lastX = event.clientX - ctx.canvas.offsetLeft;
            lastXY.lastY = event.clientY - ctx.canvas.offsetTop;

            //마우스 무브 이벤트리스너 추가
            canvas.addEventListener('mousemove', mouseMoveHandler);
        }
        //마우스를 누르지 않은 상태(마우스를 눌러 선을 긋고 땠을 때)
        else{
            superRam.push(ram);

            //push 요청 전송
            const gameRequestParam = new GameRequestParam('push');
            send(gameRequestParam);
            mouseState.isout=true;
        }
    }
}
function mouseMoveHandler(event){
    getCanvasXY(event.clientX, event.clientY);
    draw(xy[0], xy[1], lastXY.lastX, lastXY.lastY, color);
    //ram.push([xy[0], xy[1], lastXY.lastX, lastXY.lastY, color]);
    ram.push({ x: xy[0], y: xy[1], lastX: lastXY.lastX, lastY: lastXY.lastY, color: color });


    //좌표전송
    const gameRequestParam = new GameRequestParam('sendCoordinate', xy[0], xy[1], lastXY.lastX, lastXY.lastY, color);
    send(gameRequestParam);

    setLastXY(xy[0], xy[1]);
    canvas.addEventListener('mouseout', out);
}
function getCanvasXY(x, y){
    xy[0] = x - ctx.canvas.offsetLeft;
    xy[1] = y - ctx.canvas.offsetTop;
}
function draw(x, y, lastX, lastY, color){
    ctx.lineWidth = 3;
    ctx.strokeStyle = color;
    ctx.beginPath();
    ctx.moveTo(lastX, lastY);
    ctx.lineTo(x, y);
    ctx.stroke();
}
function setLastXY(xy0, xy1){
    lastXY.lastX=xy0;
    lastXY.lastY=xy1;
}
if(mouseState.isout == false){
    canvas.addEventListener('mouseup', function(event){
        canvas.removeEventListener('mousemove', mouseMoveHandler);
        canvas.removeEventListener('mouseout', out);
        mouseState.mouse = false;
        mouseHandler(event);
    });
}
function out (){
    mouseState.mouse = false;
    mouseState.isout = true;
    superRam.push(ram);
    ram=[];
    canvas.removeEventListener('mousemove', mouseMoveHandler);
    canvas.removeEventListener('mouseout', out);
}
function clear(){
    ctx.clearRect(0,0,canvas.width, canvas.height);
    superRam=[];
    //clear 요청 전송
    const gameRequestParam = new GameRequestParam('clear');
    send(gameRequestParam);
}
function clear_no_send(){
    ctx.clearRect(0,0,canvas.width, canvas.height);
    superRam = [];
}
function rollBack(){
    superRam.pop();
    ctx.clearRect(0,0,canvas.width, canvas.height);
    for(let i = 0; i<superRam.length; i++){
        for(let j = 0; j<superRam[i].length; j++){
            draw(superRam[i][j].x, superRam[i][j].y, superRam[i][j].lastX, superRam[i][j].lastY, superRam[i][j].color);
        }
    }

    //rollBack 요청 전송
    const gameRequestParam = new GameRequestParam('rollBack');
    ws.send(JSON.stringify(gameRequestParam));
}
function rollBack_no_send(){
    superRam.pop();
    ctx.clearRect(0,0,canvas.width, canvas.height);
    for(let i = 0; i<superRam.length; i++){
        for(let j = 0; j<superRam[i].length; j++){
            draw(superRam[i][j].x, superRam[i][j].y, superRam[i][j].lastX, superRam[i][j].lastY, superRam[i][j].color);
        }
    }
}
function sendAnswer(){
    const requestParam = new RequestParam('answer', '', answerBox.value);
    answerBox.value = '';
    send(requestParam);
}
function blackBtnClickHandler() { color = 'black'; };
function blueBtnClickHandler() { color = 'blue'; };
function redBtnClickHandler() { color = 'red'; };