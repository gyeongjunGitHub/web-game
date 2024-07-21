const test = document.getElementById('test');
    getMemberInfo();

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
    async function getMemberInfo(){
        const result = await getRequest('/member/getMemberInfo');
        test.innerHTML = `
            <div class='inform-wrap'>
               <div class='inform-left'>ID</div>
               <div class='inform-right'> ${result.id}</div>
            </div>
            <div class='inform-wrap'>
               <div class='inform-left'>PASSWORD</div>
               <div class='inform-right'>${result.password}</div>
            </div>
            <div class='inform-wrap'>
               <div class='inform-left'>NICK NAME</div>
               <div class='inform-right'>${result.nick_name}</div>
            </div>
            <div class='inform-wrap'>
               <div class='inform-left'>NAME</div>
               <div class='inform-right'>${result.name}</div>
            </div>
            <div class='inform-wrap'>
                <div class='inform-left'>EMAIL</div>
                <div class='inform-right'>${result.email}</div>
            </div>
            <div class='inform-wrap'>
                <div class='inform-left'>RANKING POINT</div>
                <div class='inform-right'>${result.ranking_point}</div>
            </div>
            <div class='inform-wrap'>
                <div class='inform-left'>GAME POINT</div>
                <div class='inform-right'>${result.game_point}</div>
            </div>
            <div class='inform-wrap'>
                <div class='inform-left'>프로필 사진</div>
                <div class='inform-right'><img id="dynamicImage" src="/images/${result.profilePictureDTO.stored_file_name}" width="100" height="100"></div>
            </div>
        `
    }