const side_list = [document.querySelector('.profile'), document.querySelector('.register'), document.querySelector('.shop_item_wrap'), document.querySelector('.game_setting'), document.querySelector('.member_info')]
    function set_side_list(number){
        for(let i = 0; i<side_list.length; i++){
            if(i == number){
                if(side_list[i] == document.querySelector('.shop_item_wrap')){
                    getItems();
                    side_list[i].style.display = 'grid';
                }else if(side_list[i] == document.querySelector('.game_setting')){
                    getSetting();
                    side_list[i].style.display = 'block';
                }else if(side_list[i] == document.querySelector('.member_info')){
                    getAllMember();
                    side_list[i].style.display = 'block';
                }else{
                    side_list[i].style.display = 'block';
                }
            }else{
                side_list[i].style.display = 'none';
            }
        }
    }

    //----------------------------------------------------//
    const file = document.getElementById('file');
    const itemPicture = document.getElementById('itemPicture');
    const name = document.getElementById('name');
    const price = document.getElementById('price');
    const picture_selectBtn = document.querySelector('.picture_selectBtn');
    const shop_item_wrap = document.querySelector('.shop_item_wrap');
    const shop_content = document.querySelector('.shop_content');
    const shop_modal = document.querySelector('.shop_modal');
    const game_setting = document.querySelector('.game_setting');
    const member_info = document.querySelector('.member_info');

    let member_info_list = [];
    async function getAllMember(){
        let result = await getRequest('/member/getAllMember');
        member_info_list = result;
        let tableHTML = `
            <table style="border: 1px solid black; border-collapse: collapse;">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>PASSWORD</th>
                    <th>닉네임</th>
                    <th>NAME</th>
                    <th>성별</th>
                    <th>EMAIL</th>
                    <th>랭킹포인트</th>
                    <th>게임포인트</th>
                    <th>권한</th>
                    <th>변경</th>
                    <th>삭제</th>
                </tr>
            </thead>
            <tbody>
        `;
        for (let i = 0; i < result.length; i++) {
            tableHTML += `
                <tr>
                    <td>${member_info_list[i].id}</td>
                    <td>${member_info_list[i].password}</td>
                    <td>${member_info_list[i].nick_name}</td>
                    <td>${member_info_list[i].name}</td>
                    <td>${member_info_list[i].gender}</td>
                    <td>${member_info_list[i].email}</td>
                    <td>${member_info_list[i].ranking_point}</td>
                    <td>${member_info_list[i].game_point}</td>
                    <td>${member_info_list[i].role}</td>
                    <td><button onclick="member_info_update(${i})">변경</button></td>
                    <td><button>삭제</button></td>
                </tr>
            `;
        }
        tableHTML += `
            </tbody>
            </table>
        `;
        member_info.innerHTML = tableHTML;
    }
    function member_info_update(target){
        let tableHTML = `
            <table style="border: 1px solid black; border-collapse: collapse;">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>PASSWORD</th>
                    <th>닉네임</th>
                    <th>NAME</th>
                    <th>성별</th>
                    <th>EMAIL</th>
                    <th>랭킹포인트</th>
                    <th>게임포인트</th>
                    <th>권한</th>
                    <th>변경</th>
                    <th>삭제</th>
                </tr>
            </thead>
            <tbody>
        `;
        for (let i = 0; i < member_info_list.length; i++) {
            if(i == target){
                tableHTML += `
                    <tr>
                        <td>${member_info_list[i].id}</td>
                        <td>${member_info_list[i].password}</td>
                        <td>${member_info_list[i].nick_name}</td>
                        <td>${member_info_list[i].name}</td>
                        <td>${member_info_list[i].gender}</td>
                        <td>${member_info_list[i].email}</td>
                        <td><input id="ranking_point" type="text" value = "${member_info_list[i].ranking_point}"/></td>
                        <td><input id="game_point" type="text" value = "${member_info_list[i].game_point}"/></td>
                        <td><input id="role" type="text" value = "${member_info_list[i].role}"/></td>
                        <td><button onclick="update_member_info(${i})">완료</button></td>
                    </tr>
                `;
            }else{
                tableHTML += `
                    <tr>
                        <td>${member_info_list[i].id}</td>
                        <td>${member_info_list[i].password}</td>
                        <td>${member_info_list[i].nick_name}</td>
                        <td>${member_info_list[i].name}</td>
                        <td>${member_info_list[i].gender}</td>
                        <td>${member_info_list[i].email}</td>
                        <td>${member_info_list[i].ranking_point}</td>
                        <td>${member_info_list[i].game_point}</td>
                        <td>${member_info_list[i].role}</td>
                        <td><button onclick="member_info_update(${i})">변경</button></td>
                        <td><button>삭제</button></td>
                    </tr>
                `;
            }
        }
        tableHTML += `
            </tbody>
            </table>
        `;
        member_info.innerHTML = tableHTML;
    }
    async function update_member_info(index){
        const ranking_point = document.getElementById('ranking_point');
        const game_point = document.getElementById('game_point');
        const role = document.getElementById('role');

        member_info_list[index].ranking_point = ranking_point.value;
        member_info_list[index].game_point = game_point.value;
        member_info_list[index].role = role.value;

        const url = '/member/updateMemberInfo';

        const result = postRequest(url, member_info_list[index]);
        if(result){
            alert('변경 성공!!');
            getAllMember();
        }else{
            alert('변경 실패!!');
            getAllMember();
        }
    }
    async function itemRegistration() {
      const url = '/store/itemRegistration';
      const formData = new FormData();
      formData.append('name', name.value);
      formData.append('price', price.value);
      if (itemPicture.files.length == 1) {
        formData.append('itemPicture', itemPicture.files[0]);
      }
      const result = await postFileRequest(url, formData);
      if(result){
        alert('등록 성공!!');
        name.value = '';
        price.value = '';
        itemPicture.value = '';
      }else{
        alert('등록 실패.');
        name.value = '';
        price.value = '';
        itemPicture.value = '';
      }
    }
    async function setProfile() {
        const formData = new FormData();
        if (file.files.length == 1) {
            formData.append('file', file.files[0]);
            const url = '/member/selectBasicProfile';

            const result = await postFileRequest(url, formData);
            if(result){
                alert('등록 성공!!');
                file.value = '';
            }else{
                alert('등록 실패.');
                file.value = '';
            }
        }
    }
    async function getSetting(){
        let setting = await getRequest('/game/getSetting');
        game_setting.innerHTML = '';
        for(let i = 0; i < setting.length; i++){
            game_setting.innerHTML += `
                <p>${setting[i].name} : ${setting[i].value}<button onclick="updateGameSetting('${setting[i].name}')">변경</button></p>
            `;
        }
    }
    let settingName = '';

    function updateGameSetting(id){
        settingName = id;
        game_setting.innerHTML = `<input id="round_time_value" type="text"/><button onclick="updateGameSettingDone()">완료</button>`;

    }
    async function updateGameSettingDone(){
        let value = document.getElementById('round_time_value').value;
        const result = await postRequest('/game/updateSetting', { "name" : settingName , "value" : value});
        if(result){
            alert('변경 성공!!');
            getSetting();
        }else{
            alert('변경 실패.');

        }
    }
    async function getItems() {
        items = await getRequest('/store/getItems');
        shop_item_wrap.innerHTML = '';
        for (let i = 0; i < items.length; i++) {
            const imageUrl = '/images/' + items[i].stored_file_name;
            shop_item_wrap.innerHTML += `
                <li class="shop_item">
                <div class="item_img">
                <img src="${imageUrl}" alt="" s />
                </div>
                    <p class="item_tit">${items[i].name}</p>
                    <p class="item_price">${items[i].price}</p>
                    <button class="buy_btn" onclick="showModal(${i})">아이템 삭제</button>
                </li>
            `;
        }
    }
    async function removeItem(item){
        shop_modal.style.display = 'none'
        const result = await deleteRequest('/store/removeItem', item)
        if(result){
            alert('삭제 성공!!');
            getItems();
        }else{
            alert('삭제 실패.');
        }
    }
    function showModal(i) {
        shop_modal.style.display = 'flex';
        const imageUrl = '/images/' + items[i].stored_file_name;
        shop_content.innerHTML = `
            <h2>이 아이템을 삭제하시겠습니까?</h2>
            <div class="img_box"><img class="item_image"  width="200px" height="200px" src="${imageUrl}"></div>

            <div class="item_inform">
                <div class="item_name">
                    <span>아이템이름 : </span>
                    <span>${items[i].name}</span>
                </div>
                <div class="item_price">
                    <span>아이템가격 : </span>
                    <span>${items[i].price}</span>
                </div>
            </div>
        `;
        shop_content.innerHTML += `
            <div class="modal_btn_wrap">
                <button class="buy_ok_btn" onclick="removeItem(items[${i}])">확인</button>
                <button class="buy_cancel_btn" onclick="shop_modal.style.display = 'none';">취소</button>
            </div>
        `;
    }

    async function getRequest(url = '') {
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
    //post file 요청
    async function postFileRequest(url = '', formData) {
      try {
        const response = await fetch(url, {
          method: 'POST',
          body: formData,
        });

        if (!response.ok) {
          throw new Error(
            'Network response was not ok ' + response.statusText
          );
        } else {
          return response.json();
        }
      } catch (error) {
        console.error('Fetch operation failed:', error);
      }
    }
    //post 요청
    async function postRequest(url = '', data = {}) {
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

        // JSON 형식으로 변환된 데이터를 가져옴.
        const responseData = await response.json();
        return responseData;
      } catch (error) {
        console.error('Fetch operation failed:', error);
      }
    }
    //delete 요청
    async function deleteRequest(url = '', data = {}) {
      try {
        const response = await fetch(url, {
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data),
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