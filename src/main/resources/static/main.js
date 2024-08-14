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
const finalScore_wrapBox = document.querySelector('.finalScore_wrapBox');
const finalScore_name_box = document.querySelector('.finalScore_name_box');
const finalScore_score_box = document.querySelector('.finalScore_score_box');
const matching_info = document.querySelector('.matching_info');
const input = document.querySelector('.input');
const btn = document.querySelector('.btn');



//id 변수
const memberListTable = document.getElementById('memberListTable');
const chattingArea = document.getElementById('chattingArea');
const memberNameArea = document.getElementById('memberNameArea');
const chatContentArea = document.getElementById('chatContentArea');
const message = document.getElementById('message');
const myIdArea = document.getElementById('myIdArea');
const matchingStartBtn = document.getElementById('matchingStartBtn');
const matchingCancleBtn = document.getElementById('matchingCancleBtn');
const matchingStartBtn3 = document.getElementById('matchingStartBtn3');
const matchingCancleBtn3 = document.getElementById('matchingCancleBtn3');
matchingCancleBtn3.style.display = 'none';
const matchingStartBtn4 = document.getElementById('matchingStartBtn4');
const matchingCancleBtn4 = document.getElementById('matchingCancleBtn4');
matchingStartBtn4.style.display = 'none';
matchingCancleBtn4.style.display = 'none';


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
let color = 'black'; //색깔 변수 기본 black
let turn = 0;
let xy = [];
let ram = [];
let superRam = [];
let quiz = '';
let answer = '';
let userList = [];
let userNickNameList = [];
let userProfile = [];
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

let timerId;

gameArea.style.display = 'none';
finalScore_wrapBox.style.display = 'none';


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
        const url1 = `/member/isAlreadyFriend?nick_name=${searchMemberValue.value}&myId=${myId}`;
        let isAlreadyFriend = await getRequest(url1);

        // result == 0 친구 아님, result == 1 이미 친구
        if(isAlreadyFriend.result == 0){
            const url = `/member/searchMember?nick_name=${searchMemberValue.value}`;
            let result = await getRequest(url);
            if (result.id != null) {
                searchResult.innerHTML = `
                    <div class="result-name">${result.nick_name}</div>
                    <button class="plus-btn" id = "addFriendBtn" onclick="addFriendRequest('${result.id}')">+</button>
                `;
            }
        }
        if(isAlreadyFriend.result == 1){
            const url = `/member/searchMember?nick_name=${searchMemberValue.value}`;
            let result = await getRequest(url);
            if (result.id != null) {
                searchResult.innerHTML = `
                    <div class="result-name">${result.nick_name}</div>
                    <div style="color: red; font-size:10px;">이미친구입니다.</div>
                `;
            }
        }
        if(isAlreadyFriend.result == -1){
            searchResult.innerHTML = `
                <div style="color: red; font-size:10px;">없는 유저입니다.</div>
            `;
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
    if(msg.type != undefined){
        let firstMappingParam = msg.type.split("/")[1];
        let secondMappingParam = msg.type.split("/")[2];

        if(firstMappingParam == 'member')
        {
            if(secondMappingParam == 'login'){
                for(let i = 0; i<friendList.length; i++){
                    if(msg.data == friendList[i].friend_id){
                        friendList[i].status = 'online';
                        document.querySelector('.user-menu').innerHTML = '';
                        inputFriendList(friendList)
                    }
                }
            }
            if(secondMappingParam == 'myId'){
                myId = msg.data;
            }
            if(secondMappingParam == 'myNickName'){
                myIdArea.innerHTML = `${msg.data}`;
                myNickName = msg.data;
            }
            if(secondMappingParam == 'duplicateLogin'){
                alert('다른 곳 에서 로그인 시도. 로그아웃 됩니다.');
                location.href = '/';
            }
            if(secondMappingParam == 'logout')
            {
                for(let i = 0; i<friendList.length; i++){
                    if(msg.data == friendList[i].friend_id){
                        friendList[i].status = 'offline';
                        document.querySelector('.user-menu').innerHTML = '';
                        inputFriendList(friendList);
                    }
                }
            }
            if(secondMappingParam == 'addFriendRequest')
            {
                addFriendProc(msg);
            }
        }
        if(firstMappingParam == 'matching')
        {
            if(secondMappingParam == 'userCount_2')
            {
                matching_info.innerHTML = `<p>매칭중 ..</p>(${msg.data}/2)`;
            }
            if(secondMappingParam == 'userCount_3')
            {
                matching_info.innerHTML = `<p>매칭중 ..</p>(${msg.data}/3)`;
            }
            if(secondMappingParam == 'success')
            {
                score = [0, 0, 0, 0];
                myTurn = msg.data.yourTurn;
                userList = msg.data.roomUsers;
                userNickNameList = msg.data.roomUsersNickName;

                userProfile = [];
                for(let i = 0; i<userList.length; i++){
                    userAnswerBoxList[i].style.display = 'none';
                    userScoreBoxList[i].style.backgroundColor = '';
                    if(userList[i] == myId){
                        userScoreBoxList[i].style.backgroundColor = '#99e99f';
                    }

                    const result = await getRequest(`/member/getProfile?id=${userList[i]}`);
                    userProfile.push(result.stored_file_name);
                }
                for(let i = 0; i<4; i++){
                    if(i<userNickNameList.length){
                        userAnswerBoxList[i].innerHTML = '';
                        userNameBoxList[i].innerHTML = `
                            <span>${userNickNameList[i]}</span>
                        `;

                        userScoreBoxList[i].innerHTML = `
                            <span>score : ${score[i]}</span>
                        `;
                        userPictureBoxList[i].innerHTML = `
                            <img src="/images/${userProfile[i]}" width="100" height="100">
                        `;
                    }else{
                        userAreaBoxList[i].style.display = 'none';
                    }
                }

                matchingArea.style.display = 'none';
                gameArea.style.display = 'block';
                quizBox.innerHTML = `잠시 후 게임이 시작됩니다.`;
            }
        }
        if(firstMappingParam == 'game')
        {
            if(secondMappingParam == 'rollBack')
            {
                rollBack_no_send();
            }
            if(secondMappingParam == 'clear')
            {
                clear_no_send();
            }
            if(secondMappingParam == 'push')
            {
                superRam.push(ram);
                ram = [];
            }
            if(secondMappingParam == 'sendCoordinate')
            {
                draw(msg.data[0], msg.data[1], msg.data[2], msg.data[3], msg.data[4]);
                ram.push({ x: msg.data[0], y: msg.data[1], lastX: msg.data[2], lastY: msg.data[3], color: msg.data[4] });
            }
            if(secondMappingParam == 'answer')
            {
                for(let i = 0; i < userList.length; i++){
                    if(userList[i] == msg.data.sender){
                        userAnswerBoxList[i].style.display = 'block';
                        userAnswerBoxList[i].innerHTML = `${msg.data.answer}`;

                        //타이머 실행중이면 삭제 후 다시시작
                        if(timerId){
                            clearTimeout(timerId);
                            timerId = setTimeout(() => {
                                userAnswerBoxList[i].style.display = 'none';
                            }, 2000);
                        }else{
                            timerId = setTimeout(() => {
                                userAnswerBoxList[i].style.display = 'none';
                            }, 2000);
                        }
                    }
                }
                if(msg.data.answer == answer){
                    for(let i = 0; i<userList.length; i++){
                        if(msg.data.sender == userList[i]){
                            quizBox.innerHTML = `${userNickNameList[i]}님 정답!!! -> ${answer}`;
                        }
                    }
                }
            }
            if(secondMappingParam == 'score')
            {
                score = msg.data;
                for(let i = 0; i<userList.length; i++){
                    userScoreBoxList[i].innerHTML = `
                        <span>score : ${msg.data[i].toFixed(2)}</span>
                    `;
                }
            }
            if(secondMappingParam == 'finalScore')
            {
                finalScore_wrapBox.style.display = 'flex';
                timeBox.innerHTML ='';
                finalScore_name_box.innerHTML ='';
                finalScore_score_box.innerHTML ='';
                for(let i = 0; msg.data.length; i++){
                    console.log(msg.data[i].nick_name);
                    finalScore_name_box.innerHTML += `
                        <div class="finalScore_name_txt">${msg.data[i].nick_name}</div>
                    `;
                    finalScore_score_box.innerHTML += `
                        <div class="finalScore_score_num">${msg.data[i].score.toFixed(2)}</div>
                    `;
                }
            }
            if(secondMappingParam == 'timeCount')
            {
                timeBox.innerHTML = `${msg.data}`;
            }
            if(secondMappingParam == 'quizData'){
                //퀴즈, 정답
                quiz = msg.data.quiz;
                answer = msg.data.answer;

                //내 차례
                if(msg.data.yourTurn == myTurn){
                    for(let i = 0; i<userList.length; i++){
                        if(i == msg.data.yourTurn -1){
                            userNameBoxList[i].style.color = 'blue';
                            userAreaBoxList[i].style.borderColor = 'blue';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
                        }else{
                            userNameBoxList[i].style.color = 'black';
                            userAreaBoxList[i].style.borderColor = 'black';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
                        }
                    }
                    //채팅 금지
                    sendBtn.removeEventListener('click', sendAnswer);
                    document.removeEventListener('keypress', sendAnswerKeyPress);
                    //그리기 이벤트 리스너 추가
                    addListener();
                    //quiz 출력
                    quizBox.innerHTML = `${answer}`;

                    const data = new Data('/game/start');
                    send(data);
                }else{
                    for(let i = 0; i<userList.length; i++){
                        if(i == msg.data.yourTurn -1){
                            userNameBoxList[i].style.color = 'blue';
                            userAreaBoxList[i].style.borderColor = 'blue';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span><button onclick="ttabong(${[i]})">👍</button>`;
                        }else{
                            userNameBoxList[i].style.color = 'black';
                            userAreaBoxList[i].style.borderColor = 'black';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
                        }
                    }
                    //채팅 활성화
                    sendBtn.addEventListener('click', sendAnswer);
                    document.addEventListener('keypress', sendAnswerKeyPress);

                    //그리기 이벤트 리스너 삭제
                    removeListener();
                    //quiz 출력
                    quizBox.innerHTML = `${quiz}`;
                }
            }
            if(secondMappingParam == 'nextTurn'){
                //퀴즈, 정답
                quiz = msg.data.quiz;
                answer = msg.data.answer;

                //자기 자신의 턴 일 경우
                if(msg.data.yourTurn == myTurn){
                    for(let i = 0; i<userList.length; i++){
                        if(i == msg.data.yourTurn -1){
                            userNameBoxList[i].style.color = 'blue';
                            userAreaBoxList[i].style.borderColor = 'blue';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
                        }else{
                            userNameBoxList[i].style.color = 'black';
                            userAreaBoxList[i].style.borderColor = 'black';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
                        }
                    }
                    //채팅 금지
                    sendBtn.removeEventListener('click', sendAnswer);
                    document.removeEventListener('keypress', sendAnswerKeyPress);
                    //그리기 이벤트 리스너 추가
                    addListener();

                    //quiz 출력
                    quizBox.innerHTML = `${answer}`;

                    const data = new Data('/game/startRound');
                    send(data);

                }else{
                    for(let i = 0; i<userList.length; i++){
                        if(i == msg.data.yourTurn -1){
                            userNameBoxList[i].style.color = 'blue';
                            userAreaBoxList[i].style.borderColor = 'blue';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span><button onclick="ttabong(${i})">👍</button>`;
                        }else{
                            userNameBoxList[i].style.color = 'black';
                            userAreaBoxList[i].style.borderColor = 'black';
                            userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
                        }
                    }
                    //채팅 활성화
                    sendBtn.addEventListener('click', sendAnswer);
                    document.addEventListener('keypress', sendAnswerKeyPress);

                    //그리기 이벤트 리스너 삭제
                    removeListener();

                    //quiz 출력
                    quizBox.innerHTML = `${quiz}`;

                }
            }
            if(secondMappingParam == 'leaveMember')
            {
                for(let i = 0; i < userNickNameList.length; i++){
                    if(userNickNameList[i] == msg.data){
                        userScoreBoxList[i].style.backgroundColor = 'red';
                        userNameBoxList[i].innerHTML = '탈주';
                    }
                }
            }
            if(secondMappingParam == 'alone')
            {
                alert('게임 인원이 부족하여 종료됩니다.');
                matchingArea.style.display = 'flex';
                gameArea.style.display = 'none';
                finalScore_wrapBox.style.display = 'none';
                matchingStartBtn.style.display = 'block';
                matchingStartBtn3.style.display = 'block';
                matchingCancleBtn.style.display = 'none';
                matchingCancleBtn3.style.display = 'none';
                matching_info.style.display = 'none';
            }
        }
        if(firstMappingParam == 'chatting')
        {
            if(secondMappingParam == 'sendChatting')
            {
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
        }
    }
    if (Array.isArray(msg)) {
        if(msg[0].type.split("/")[1] == 'friend'){
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
}
//따봉!!
function ttabong(i){
    userNameBoxList[i].innerHTML = `<span>${userNickNameList[i]}</span>`;
    const data = new Data('/game/ttabong', userNickNameList[i]);
    send(data);
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
        const data = new Data('/matching/start/2');
        send(data);
        matchingStartBtn.style.display = 'none';
        matchingStartBtn3.style.display = 'none';
        matchingCancleBtn.style.display = 'block';
        matching_info.style.display = 'block';
    }
    if (request == 'drowGameCancle') {
        const data = new Data('/matching/cancel/2');
        send(data);

        matchingStartBtn.style.display = 'block';
        matchingStartBtn3.style.display = 'block';
        matchingCancleBtn.style.display = 'none';
        matching_info.style.display = 'none';
    }
}
function matching3(request){
    if(request == 'drowGameStart3'){
        const data = new Data('/matching/start/3');
        send(data);
        matchingStartBtn.style.display = 'none';
        matchingStartBtn3.style.display = 'none';
        matchingCancleBtn3.style.display = 'block';
        matching_info.style.display = 'block';
    }
    if (request == 'drowGameCancle3') {
        const data = new Data('/matching/cancel/3');
        send(data);
        matchingStartBtn.style.display = 'block';
        matchingStartBtn3.style.display = 'block';
        matchingCancleBtn3.style.display = 'none';
        matching_info.style.display = 'none';
    }
}

// 친구 추가 요청 전송
function addFriendRequest(id) {
    const data = new Data('/member/addFriendRequest', { receiver : id });
    send(data);
    document.getElementById('addFriendBtn').style.display = 'none';
    searchResult.innerHTML = `
        <div class="result-name">${id}</div>
        <div style="color: green; font-size:10px;">요청 전송 완료.</div>
    `;
}
// 친구 추가 요청 응답 전송 -> 수락 및 거절 이후 화면 처리 필요
function addFriendResponse(response, receiver){
    const data = new Data('/member/addFriendResponse', { 'receiver' : receiver , 'response' : response });
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
        let hour = chattingData[i].date.split('T')[1].split(':')[0];
        let minute = chattingData[i].date.split('T')[1].split(':')[1];
        let time = hour+':'+minute;
        if(chattingData[i].sender == myId){
            chatContentArea.innerHTML += `
                <div class="chatBallonArea">

                    <p class="chat-ballon1">${chattingData[i].content}<span class="chat-time1">${time}</span></p>
                </div>
            `;
        }
        if(chattingData[i].sender == getMember()){
            chatContentArea.innerHTML += `
                <div class="chatBallonArea">

                    <p class="chat-ballon2">${chattingData[i].content}<span class="chat-time2">${time}</span></p>
                </div>
            `;
        }
    }
    chatContentArea.scrollTop = chatContentArea.scrollHeight;
}
//메시지 set
function sendMessage() {
    const data = new Data('/chatting/sendChatting', { 'receiver' : getMember(), 'content' : message.value});
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
            const data = new Data('/game/push');
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
    const data = new Data('/game/sendCoordinate', [xy[0], xy[1], lastXY.lastX, lastXY.lastY, color]);
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
    const data = new Data('/game/clear');
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
    const data = new Data('/game/rollBack');
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
    const data = new Data('/game/answer', { "answer" : answerBox.value , "timeCount" : timeBox.innerHTML});
    answerBox.value = '';
    send(data);
}
function sendAnswerKeyPress(e){
    if(e.keyCode == 13){ //enter press
        const data = new Data('/game/answer', { "answer" : answerBox.value , "timeCount" : timeBox.innerHTML});
        answerBox.value = '';
        send(data);
    }
}
function blackBtnClickHandler() { color = 'black'; };
function blueBtnClickHandler() { color = 'blue'; };
function redBtnClickHandler() { color = 'red'; };
function goMain(){
    matchingArea.style.display = 'flex';
    gameArea.style.display = 'none';
    finalScore_wrapBox.style.display = 'none';
    matchingStartBtn.style.display = 'block';
    matchingStartBtn3.style.display = 'block';
    matchingCancleBtn.style.display = 'none';
    matchingCancleBtn3.style.display = 'none';
    matching_info.style.display = 'none';
}