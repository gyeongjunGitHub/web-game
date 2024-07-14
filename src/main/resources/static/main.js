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
const userScoreBoxList = [document.querySelector('.user1_score'), document.querySelector('.user2_score'), document.querySelector('.user3_score'), document.querySelector('.user4_score')]
const userPictureBoxList = [document.querySelector('.user1_picture'), document.querySelector('.user2_picture'), document.querySelector('.user3_picture'), document.querySelector('.user4_picture')]
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
let myNickName = '';
let chattingAreaMemberName = '';
let chattingAreaIsTrue = false;
let memberList = [];
let friendList = [];
let chattingDataList = [];
let chattingIsReadFalseCount = [];
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
let userNickNameList = [];
let score = [];
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
class Friend{
    #member_id;
    #friend_id;
    #friend_nick_name;
    #status;

    constructor(data){
        this.#member_id = data.member_id;
        this.#friend_id = data.friend_id;
        this.#friend_nick_name = data.friend_nick_name;
        this.#status = data.status;
    }

    get member_id(){ return this.#member_id; }
    get friend_id(){ return this.#friend_id; }
    get friend_nick_name(){ return this.#friend_nick_name; }
    get status(){ return this.#status; }
    set status(status){ this.#status = status; }
}
//GET 요청 함수
async function getRequest(url = '') {
    try {
        const response = await fetch(url, { method: 'GET' });
        if (!response.ok) {
            throw new Error(
                'Network response was not ok ' + response.statusText
            );
        }
        const text = await response.text();
        // 응답이 비어 있지 않은지 확인
        if (text.length != 0) {
            const responseData = JSON.parse(text);
            return responseData;
        }
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
function Data(type='', data = ''){
    this.type = type;
    this.data = data;
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
async function receiveMessageHandler(msg) {
    if(msg.type == 'addFriendResponse'){
        if(msg.data == 'true'){
            document.querySelector('.user-menu').innerHTML = '';
            inputFriendList(friendList);
        }
    }
    if(msg.type == 'leaveOtherMember'){
        alert('다른 유저가 게임을 떠났습니다.');
        location.href = '/main';
    }
    if(msg.type == 'answer'){
        for(let i = 0; i < userList.length; i++){
            if(userList[i] == msg.data.sender){
                userAnswerBoxList[i].innerHTML = `${msg.data.answer}`;
            }
        }
        if(msg.data.answer == answer){
            for(let i = 0; i<userList.length; i++){
                if(msg.data.sender == userList[i]){
                    quizBox.innerHTML = `${userNickNameList[i]}님 정답!!! -> ${answer}`;
                    score[i] += 10;
                    userScoreBoxList[i].innerHTML = `
                        <span>score : ${score[i]}</span>
                    `;
                }
            }
            time = 5;
        }
    }
    if(msg.type == 'gameOver'){
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
    if(msg.type == 'timeCount'){ timeBox.innerHTML = `${msg.data.timeCount}`; }
    if(msg.type == 'gameStart' || msg.type == 'nextTurn'){
        quiz = msg.data.quiz;
        answer = msg.data.answer;
        if(msg.data.yourTurn == myTurn){
            sendBtn.removeEventListener('click', sendAnswer);
            if(myTurn == 2){ cycle++; }
            addListener();
            time = 60;
            quizBox.innerHTML = `${answer}`;
            timeBox.innerHTML = `${time}`;
            const data = new Data('timeCount', '', time);
            timeCalculation1();
            function timeCalculation1(){
                setTimeout(function() {
                    if(time > 1){
                        time--;
                        data.data = {"timeCount" : time };
                        send(data);
                        timeCalculation1();
                    }else{
                        if(cycle == 2 && myTurn == 2){
                            data.type = 'gameOver';
                            send(data);
                            cycle = 0;
                        }else{
                            removeListener();
                            const data = new Data('nextTurn', {"myTurn" : myTurn});
                            send(data);
                            clear();
                            clear_no_send();
                        }
                    }
                }, 1000);
            }
        }else{
            sendBtn.addEventListener('click', sendAnswer);
            quizBox.innerHTML = `${quiz}`;
        }
    }
    if(msg.type == 'rollBack'){ rollBack_no_send(); }
    if(msg.type == 'push'){
        superRam.push(ram);
        ram = [];
    }
    if(msg.type == 'clear'){ clear_no_send(); }
    if(msg.type == 'sendCoordinate'){
        draw(msg.data[0], msg.data[1], msg.data[2], msg.data[3], msg.data[4]);
        ram.push({ x: msg.data[0], y: msg.data[1], lastX: msg.data[2], lastY: msg.data[3], color: msg.data[4] });
    }
    if(msg.type == 'matchingStartDrowGame' && msg.data.response == 'success'){
        score = [0, 0, 0, 0];
        myTurn = msg.data.yourTurn;
        userList = msg.data.roomUsers;
        userNickNameList = msg.data.roomUsersNickName;

        let userProfile = [];
        for(let i = 0; i<userList.length; i++){
            const result = await getRequest(`/member/getProfile?id=${userList[i]}`);
            userProfile.push(result.stored_file_name);
        }
        for(let i = 0; i<4; i++){
            if(i<userNickNameList.length){
                userAnswerBoxList[i].innerHTML = '';
                userNameBoxList[i].innerHTML = `
                    <span>${userNickNameList[i]}</span>
                `;
                if(myId != userList[i]){
                    userNameBoxList[i].innerHTML += `<button onclick="ttabong('${userNickNameList[i]}')">👍</button>`;
                }
                userScoreBoxList[i].innerHTML = `
                    <span>score : ${score[i]}</span>
                `;
                userPictureBoxList[i].innerHTML = `
                    <img src="/images/${userProfile[i]}" width="200" height="200">
                `;
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
                    const data = new Data('gameStart');
                    send(data);
                }
            }, 1000);
        }
    }
    if(msg.type == 'addFriendRequest'){ addFriendProc(msg); }
    if (msg.type == 'sendMessage') {
        if (chattingAreaIsTrue){
            if(msg.sender == getMember() && msg.receiver == myId){
                //c.getReceiver().equals(myId)
                getRequest(`/member/setIsRead?member_id=${getMember()}`);
                msg.receiver_is_read = true;
                for(let i = 0; i < friendList.length; i++){
                    if(msg.sender == friendList[i].friend_id){
                        chattingDataList[i].push(msg);
                        chatContentArea.innerHTML = '';
                        inputChattingData(chattingDataList[i]);
                    }
                }
            }
            else if(msg.sender == myId && msg.receiver == getMember()){
                for(let i = 0; i < friendList.length; i++){
                    if(msg.receiver == friendList[i].friend_id){
                        chattingDataList[i].push(msg);
                        chatContentArea.innerHTML = '';
                        inputChattingData(chattingDataList[i]);
                    }
                }
            }
            else{
                for(let i = 0; i<friendList.length; i++){
                    if(msg.sender == myId && msg.receiver == friendList[i].friend_id){
                        chattingDataList[i].push(msg);
                    }
                    if(msg.sender == friendList[i].friend_id && msg.receiver == myId){
                        chattingDataList[i].push(msg);
                    }
                }

                chattingIsReadFalseCount = [];
                for(let i = 0; i<chattingDataList.length; i++){
                    let count = 0;
                    for(let j = 0; j<chattingDataList[i].length; j++){
                        if(chattingDataList[i][j].receiver == myId && chattingDataList[i][j].receiver_is_read == false){
                            count++;
                        }
                    }
                    chattingIsReadFalseCount.push(count);
                }
                document.querySelector('.user-menu').innerHTML = '';
                inputFriendList(friendList);
            }
        }
        else{
            for(let i = 0; i<friendList.length; i++){
                if(msg.sender == myId && msg.receiver == friendList[i].friend_id){
                    chattingDataList[i].push(msg);
                }
                if(msg.sender == friendList[i].friend_id && msg.receiver == myId){
                    chattingDataList[i].push(msg);
                }
            }

            chattingIsReadFalseCount = [];
            for(let i = 0; i<chattingDataList.length; i++){
                let count = 0;
                for(let j = 0; j<chattingDataList[i].length; j++){
                    if(chattingDataList[i][j].receiver == myId && chattingDataList[i][j].receiver_is_read == false){
                        count++;
                    }
                }
                chattingIsReadFalseCount.push(count);
            }
            document.querySelector('.user-menu').innerHTML = '';
            inputFriendList(friendList);
        }
    }
    if (Array.isArray(msg)) {
        if(msg[0].type == 'friend'){
            chattingIsReadFalseCount = [];
            chattingDataList = [];
            friendList = [];
            for(let i = 0; i<msg.length; i++){
                const friend = new Friend(msg[i].data);
                friendList.push(friend);
            }
        }
        for(let i = 0; i<friendList.length; i++){
            let result = await getRequest(`/member/getChatting?member_id=${friendList[i].friend_id}`);
            chattingDataList.push(result);
        }
        for(let i = 0; i<chattingDataList.length; i++){
            let count = 0;
            for(let j = 0; j<chattingDataList[i].length; j++){
                if(chattingDataList[i][j].receiver == myId && chattingDataList[i][j].receiver_is_read == false){
                    count++;
                }
            }
            chattingIsReadFalseCount.push(count);
        }
        document.querySelector('.user-menu').innerHTML = '';
        inputFriendList(friendList);
    }
    if(msg.type == 'myId') {
        myIdArea.innerHTML = `${msg.data}`;
        myId = msg.data;
    }
    if(msg.type == 'myNickName'){ myNickName = msg.data; }
    if(msg.type == 'login'){
        for(let i = 0; i<friendList.length; i++){
            if(msg.data == friendList[i].friend_id){
                friendList[i].status = 'online';
                document.querySelector('.user-menu').innerHTML = '';
                inputFriendList(friendList)
            }
        }
    }
    if (msg.type == "logout") {
        for(let i = 0; i<friendList.length; i++){
            if(msg.data == friendList[i].friend_id){
                friendList[i].status = 'offline';
                document.querySelector('.user-menu').innerHTML = '';
                inputFriendList(friendList);
            }
        }
    }
    if(msg.type == 'duplicateLogin'){
        alert('다른 곳 에서 로그인 시도. 로그아웃 됩니다.');
        location.href = '/';
    }
}
//따봉!!
function ttabong(nick_name){
    console.log(nick_name);

}
//친구추가 요청 처리 함수
function addFriendProc(msg){
    acceptArea.innerHTML = `
        <div class="accept-msg-area">
            <p class="accept-msg">${msg.data.sender} 님이</p>
            <p class="accept-msg">친구추가 요청을 보냈습니다.</p>
            <div class="accept-check">
                <div class="yes" onclick="addFriendResponse(true, '${msg.data.sender}')">yes</div>
                <div class="no" onclick="addFriendResponse(false, '${msg.data.sender}')">no</div>
            </div>
        </div>
    `;
    acceptArea.style.display = 'block';
}
// 매칭 함수
function matching(request) {
    if (request == 'drowGameStart') {
        const data = new Data('matchingStartDrowGame');
        send(data);
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
    const data = new Data('addFriendRequest', { receiver : id });
    send(data);
}
// 친구 추가 요청 응답 전송 -> 수락 및 거절 이후 화면 처리 필요
function addFriendResponse(response, receiver){
    const data = new Data('addFriendResponse', { 'receiver' : receiver , 'response' : response });
    send(data);
    acceptArea.style.display='none';
}
//친구목록 출력
function inputFriendList(friendList){
    for(let i = 0; i < friendList.length; i++){
        if(friendList[i].status == 'online'){
            setMemberStateOnline(friendList[i].friend_id, friendList[i].friend_nick_name);
        }
        if(friendList[i].status == 'offline'){
            setMemberStateOffline(friendList[i].friend_id, friendList[i].friend_nick_name);
        }
    }
}
//유저 온라인 상태 set
function setMemberStateOnline(id, nick_name){
    for(let i = 0; i<friendList.length; i++){
        if(id == friendList[i].friend_id){
            if(chattingIsReadFalseCount[i] == 0){
                document.querySelector('.user-menu').innerHTML +=
                    `
                        <li class="user-list" onclick="chat('${id}')">
                            <div class="user-name">${nick_name}</div>
                            <div class="user-online"></div>
                        </li>
                    `
                ;
            }
            else{
                document.querySelector('.user-menu').innerHTML +=
                    `
                        <li class="user-list" onclick="chat('${id}')">
                            <div class="user-name">${nick_name} <span class="chat-alarm">${chattingIsReadFalseCount[i]}</span></div>
                            <div class="user-online"></div>
                        </li>
                    `
                ;
            }
        }
    }
}
//유저 오프라인 상태 set
function setMemberStateOffline(id, nick_name){
    for(let i = 0; i<friendList.length; i++){
        if(id == friendList[i].friend_id){
            if(chattingIsReadFalseCount[i] == 0){
                document.querySelector('.user-menu').innerHTML +=
                    `
                        <li class="user-list" onclick="chat('${id}')">
                            <div class="user-name">${nick_name}</div>
                            <div class="user-offline"></div>
                        </li>
                    `
                ;
            }
            else{
                document.querySelector('.user-menu').innerHTML +=
                    `
                        <li class="user-list" onclick="chat('${id}')">
                            <div class="user-name">${nick_name} <span class="chat-alarm">${chattingIsReadFalseCount[i]}</span></div>
                            <div class="user-offline"></div>
                        </li>
                    `
                ;
            }
        }
    }
}
//현재 열려있는 채팅창의 member 를 저장
let member;
function getMember() { return member; }
function setMember(m) { member = m; }
async function chat(member) {
    setMember(member);
    //채팅 공간 토글 처리
    if (!chattingAreaIsTrue && chattingAreaMemberName != member) {
        chattingAreaMemberName = member;
        chattingAreaIsTrue = !chattingAreaIsTrue;
    }else if(chattingAreaIsTrue && chattingAreaMemberName != member){
        chattingAreaMemberName = member;
    }else{
        chattingAreaIsTrue = !chattingAreaIsTrue;
    }

    for(let i = 0; i<friendList.length; i++){
        if(member == friendList[i].friend_id){
            memberNameArea.innerHTML = friendList[i].friend_nick_name;
        }
    }

    //채팅 공간이 열려있을 때
    if (chattingAreaIsTrue) {
        getRequest(`/member/setIsRead?member_id=${getMember()}`);
        for(let i = 0; i < friendList.length; i++){
            if(getMember() == friendList[i].friend_id){
                chatContentArea.innerHTML = '';
                for(let j = 0; j < chattingDataList[i].length; j++){
                    if(chattingDataList[i][j].receiver == myId && chattingDataList[i][j].receiver_is_read != true){
                        chattingDataList[i][j].receiver_is_read = true;
                    }
                }
                inputChattingData(chattingDataList[i]);
            }
        }

        chattingIsReadFalseCount = [];
        for(let i = 0; i<chattingDataList.length; i++){
            let count = 0;
            for(let j = 0; j<chattingDataList[i].length; j++){
                if(chattingDataList[i][j].receiver == myId && chattingDataList[i][j].receiver_is_read == false){
                    count++;
                }
            }
            chattingIsReadFalseCount.push(count);
        }
        document.querySelector('.user-menu').innerHTML = '';
        inputFriendList(friendList);

        chattingArea.style.display = 'block';
        chatContentArea.scrollTop = chatContentArea.scrollHeight;
    } else {
        chattingArea.style.display = 'none';
        setMember('');
    }


}
function chattingAreaClose(){
    chattingArea.style.display = 'none';
    chattingAreaIsTrue = false;
}
function inputChattingData(chattingData){
    //채팅 데이터 입력
    for(let i = 0; i < chattingData.length; i++){
        if(chattingData[i].sender == myId){
            chatContentArea.innerHTML += `
                <div class="chatBallonArea">
                    <p class="chat-ballon1">${chattingData[i].content}</p>
                </div>
            `;
        }
        if(chattingData[i].sender == getMember()){
            chatContentArea.innerHTML += `
                <div class="chatBallonArea">
                    <p class="chat-ballon2">${chattingData[i].content}</p>
                </div>
            `;
        }
    }
    chatContentArea.scrollTop = chatContentArea.scrollHeight;
}
//메시지 set
function sendMessage() {
    const data = new Data('sendMessage', { 'receiver' : getMember(), 'content' : message.value});
    send(data);
    message.value = '';
}
//메시지 전송
function send(requestParam) {
    ws.send(JSON.stringify(requestParam));
}
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
            const data = new Data('push');
            send(data);
            mouseState.isout=true;
        }
    }
}
function mouseMoveHandler(event){
    getCanvasXY(event.clientX, event.clientY);
    draw(xy[0], xy[1], lastXY.lastX, lastXY.lastY, color);
    ram.push({ x: xy[0], y: xy[1], lastX: lastXY.lastX, lastY: lastXY.lastY, color: color });


    //좌표전송
    const data = new Data('sendCoordinate', [xy[0], xy[1], lastXY.lastX, lastXY.lastY, color]);
    send(data);

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
    const data = new Data('clear');
    send(data);
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
    const data = new Data('rollBack');
    ws.send(JSON.stringify(data));
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
    const data = new Data('answer', { "answer" : answerBox.value });
    answerBox.value = '';
    send(data);
}
function blackBtnClickHandler() { color = 'black'; };
function blueBtnClickHandler() { color = 'blue'; };
function redBtnClickHandler() { color = 'red'; };