const id = document.getElementById('id');
    const password = document.getElementById('password');
    const messageBox = document.getElementById('messageBox');

    //로그인 파라미터 객체
    function LoginParam(id, password) {
      this.id = id;
      this.password = password;
    }

    //로그인
    async function login() {
      const url = '/member/loginProc';
      const idReg = /^[a-zA-Z0-9]*$/g;
      const passwordReg = /^[a-zA-Z0-9!@#$%^&*()]*$/g;
      const idPattern = idReg.test(id.value);
      const pwPattern = passwordReg.test(password.value);

      if (id.value == '' || password.value == '') {
        alert('입력하지 않은 곳이 있습니다');
      } else if (!pwPattern && !idPattern) {
        alert('아이디와 비밀번호를 확인하세요');
        id.value = '';
        password.value = '';
      } else if (pwPattern && !idPattern) {
        alert('아이디를 확인하세요');
        id.value = '';
        password.value = '';
      } else if (!pwPattern && idPattern) {
        alert('비밀번호를 확인하세요');
        id.value = '';
        password.value = '';
      } else {

        const loginParam = new LoginParam(id.value, password.value);
        let result = await postRequest(url, loginParam);

        if (result == 0) {
            messageBox.innerHTML = `<span style="color: red">로그인 실패.</span>`;
            id.value='';
            password.value = '';
        } else {
            location.href = '/main';
        }
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

        return responseData.result;
      } catch (error) {
        console.error('Fetch operation failed:', error);
      }
    }