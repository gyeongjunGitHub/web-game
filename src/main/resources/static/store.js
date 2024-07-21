const shop_item_wrap = document.querySelector('.shop_item_wrap');
    const shop_content = document.querySelector('.shop_content');
    const shop_modal = document.querySelector('.shop_modal');
    const game_point = document.querySelector('.game_point');
    let items = [];
    let myGamePoint;
    //아이템 목록 요청
    getMyGamePoint();
    getItems();

    async function getMyGamePoint(){
        myGamePoint = await Request('GET', '/member/getMyGamePoint');
        game_point.innerHTML = `내 게임포인트 : ${myGamePoint}`;
    }
    async function getItems() {
        items = await Request('GET', '/store/getItems');
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
                    <button class="buy_btn" onclick="showModal(${i})">구매하기</button>
                </li>
            `;
        }
    }
    async function buy(item){
        shop_modal.style.display = 'none'
        const result = await Request('POST', '/store/buy', item)
        if(result){
            alert('구매 성공!!');
            getMyGamePoint();
        }else{
            alert('구매 실패.');
        }
    }
    function showModal(i) {
        shop_modal.style.display = 'flex';
        const imageUrl = '/images/' + items[i].stored_file_name;
        shop_content.innerHTML = `
            <h2>이 아이템을 구매하시겠습니까?</h2>
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
        if(myGamePoint >= items[i].price){
            shop_content.innerHTML += `
                <div class="modal_btn_wrap">
                    <button class="buy_ok_btn" onclick="buy(items[${i}])">확인</button>
                    <button class="buy_cancel_btn" onclick="shop_modal.style.display = 'none';">취소</button>
                </div>
            `;
        }else{
            shop_content.innerHTML += `
                <div class="modal_btn_wrap">
                    <span style="font-size : 22px; color :  red; display: inline-block; margin-top:15px;">게임 포인트 부족.</span><br>
                    <button class="buy_cancel_btn" onclick="shop_modal.style.display = 'none';">취소</button>
                </div>
            `;
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
                return responseData;
            } catch (error) {
                console.error('Fetch operation failed:', error);
            }
        }
    }