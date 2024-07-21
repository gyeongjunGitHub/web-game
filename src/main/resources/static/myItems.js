const shop_item_wrap = document.querySelector('.shop_item_wrap');
    const shop_content = document.querySelector('.shop_content');
    const shop_modal = document.querySelector('.shop_modal');

    let myItems = [];
    let nickNameIsTrue = false;

    //내 아이템 목록 요청
    getMyItems();
    async function getMyItems(){
        myItems = await Request('GET', '/member/getMyItems');
        shop_item_wrap.innerHTML = '';
        for (let i = 0; i < myItems.length; i++) {
            if(myItems[i].count > 0){
                const imageUrl = '/images/' + myItems[i].stored_file_name;
                shop_item_wrap.innerHTML += `
                    <li class="shop_item">
                    <div class="item_img">
                    <img src="${imageUrl}" alt="" s />
                    </div>
                        <p class="item_tit">${myItems[i].name} X ${myItems[i].count}</p>
                        <button class="buy_btn" onclick="showModal(${i})">사용하기</button>
                    </li>
                `;
            }
        }
    }
    function showModal(i) {
        shop_modal.style.display = 'flex';
        const imageUrl = '/images/' + myItems[i].stored_file_name;
        shop_content.innerHTML = `
            <h2>이 아이템을 사용하시겠습니까?</h2>
            <div class="img_box"><img class="item_image"  width="200px" height="200px" src="${imageUrl}"></div>
            <div class="item_inform">
                <div class="item_name">
                    <span>아이템이름 : </span>
                    <span>${myItems[i].name}</span>
                </div>
            </div>
            <div class="modal_btn_wrap">
                <button class="buy_ok_btn" onclick="itemUse(myItems[${i}])">사용</button>
                <button class="buy_cancel_btn" onclick="shop_modal.style.display = 'none';">취소</button>
            </div>
        `;
    }
    function itemUse(myItem){
        if(myItem.name == '프로필 사진 변경권'){
            shop_content.innerHTML = `
                <h2>프로필 사진 변경</h2>
                <img id="imagePreview" src="#" alt="Image Preview" style="display: none; width: 100px; height: 100px; margin:0 auto;">
                <div style="text-align: center;">
                    <input type="file" id="fileInput" style="margin-left:100px;margin-top:20px;" multiple>

                </div>
                <div class="modal_btn_wrap">
                    <button class="buy_ok_btn" onclick="selectDone()">선택 완료</button>
                    <button class="buy_cancel_btn" onclick="shop_modal.style.display = 'none';">취소</button>
                </div>
            `;

            document.getElementById('fileInput').addEventListener('change', function(event) {
                const file = event.target.files[0];
                if (!file) return;

                const imagePreview = document.getElementById('imagePreview');
                const fileURL = URL.createObjectURL(file);

                if (file.type.startsWith('image/')) {
                    imagePreview.style.display = 'block';
                    imagePreview.src = fileURL;
                } else {
                    imagePreview.style.display = 'none';
                    alert('이미지 파일을 선택하세요.');
                }
            });
        }
        if(myItem.name == '닉네임 변경권'){
            shop_content.innerHTML = `
                <h2>닉네임 변경</h2>
                <div id="nickNameMessageBox"></div>
                <input type="text" placeholder="닉네임" id="nick_name"/>
                <div class="modal_btn_wrap">
                    <button class="buy_ok_btn" onclick="updateNickName()">선택 완료</button>
                    <button class="buy_cancel_btn" onclick="shop_modal.style.display = 'none';">취소</button>
                </div>
            `;
        }
        const nick_name = document.getElementById('nick_name');
        nick_name.addEventListener('keyup', nickNameDuplicateCheck);
    }


    async function nickNameDuplicateCheck(){
        let url = '/member/nickNameDuplicateCheck';
        const nickNameMessageBox = document.getElementById('nickNameMessageBox');
        let checkParam = {
            nick_name: nick_name.value,
        };
        let result = await Request('POST', url, checkParam);
        if (result == 1) {
            //사용 가능
            nickNameIsTrue = true;
            nickNameMessageBox.innerHTML = `<span style="color: green;">사용 가능한 닉네임 입니다.</span>`;
        } else if (result == 0) {
            //사용 불가능
            nickNameIsTrue = false;
            nickNameMessageBox.innerHTML = `<span style="color: red;">존재하는 닉네임 입니다.</span>`;
        }
    }
    async function updateNickName(){
        const url = '/member/updateNickName';
        const nick_name = document.getElementById('nick_name');
        const formData = new FormData();

        if(nickNameIsTrue){

            formData.append('nick_name', nick_name.value)
            let result = await postFileRequest(url, formData);
            console.log(result);
            if(result){
                alert('변경 완료.');
                shop_modal.style.display = 'none';
                getMyItems();
            }else{
                alert('변경 실패');
                shop_modal.style.display = 'none';
            }
        }else{
            alert('중복체크바람');
        }
    }
    async function selectDone(){
        const fileInput = document.getElementById('fileInput');
        const url = '/member/updateProfilePicture';

        const formData = new FormData();
        if(fileInput.files.length == 0){
            alert('이미지 파일을 선택하세요.');
        }
        if(fileInput.files.length == 1){
            formData.append('picture', fileInput.files[0]);
        }
        const result = await postFileRequest(url, formData);
        if(result == 1){
            shop_modal.style.display = 'none';
            getMyItems();
        }
    }
    async function Request(method, url, data = ''){
        if(method == 'GET'){
            try {
                const response = await fetch(url, { method: 'GET' });
                if (!response.ok) {
                    throw new Error(
                        'Network response was not ok ' + response.statusText
                    );
                }
                const responseData = await response.json();
                return responseData;
            } catch (error) {
                console.error('Fetch operation failed:', error);
            }
        }
        if(method == 'POST'){
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data),
                });

                if (!response.ok) {
                    throw new Error(
                        'Network response was not ok ' + response.statusText
                    );
                }

                const responseData = await response.json();
                return responseData.result;
            } catch (error) {
                console.error('Fetch operation failed:', error);
            }
        }
    }
    //post file 요청
    async function postFileRequest(url = '', formData) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                body: formData
            });

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