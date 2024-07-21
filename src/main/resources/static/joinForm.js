//member 변수
    const id = document.getElementById('id');
    const password = document.getElementById('password');
    const nick_name = document.getElementById('nick_name');
    const name = document.getElementById('name');
    const genderRadios = document.querySelectorAll('input[name="gender"]');
    const email = document.getElementById('email');
    const idMessageBox = document.getElementById('idMessageBox');
    const nickNameMessageBox = document.getElementById('nickNameMessageBox');
    const warn = document.querySelectorAll('.warn');

    let idIsTrue = false;
    let nickNameIsTrue = false;

    id.addEventListener('keyup',idDuplicateCheck);
    nick_name.addEventListener('keyup', nickNameDuplicateCheck);

    async function idDuplicateCheck(){
        let url = '/member/idDuplicateCheck';
        let checkParam = {
            id: id.value,
        };
        let result = await postRequest(url, checkParam);

        if (result == 1) {
            //사용 가능
            idIsTrue = true;
            idMessageBox.innerHTML = `<span style="color: green;">사용 가능한 아이디 입니다.</span>`;
        } else if (result == 0) {
            //사용 불가능
            idIsTrue = false;
            idMessageBox.innerHTML = `<span style="color: red;">존재하는 아이디 입니다.</span>`;
        }
    }
    async function nickNameDuplicateCheck(){
        let url = '/member/nickNameDuplicateCheck';
        let checkParam = {
            nick_name: nick_name.value,
        };
        let result = await postRequest(url, checkParam);
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


    //Member 생성자
    function Member(id, password, nick_name, name, gender, email) {
      this.id = id;
      this.password = password;
      this.nick_name = nick_name;
      this.name = name;
      this.gender = gender;
      this.email = email;
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





    async function join() {
      let isCorrect = 0;
      const idReg = /^[a-zA-Z0-9]*$/g;
      const passwordReg = /^[a-zA-Z0-9!@#$%^&*()]*$/g;
      const nick_nameReg = /^[a-zA-Z0-9가-힣]*$/g;;
      const nameReg = /^[가-힣]{1,4}$/;
      const emailReg =
        /^[0-9a-zA-Z]([-_\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;

      if (!idReg.test(id.value)) {
        warn[0].innerHTML = '형식이 맞지 않습니다.';
        warn[0].style.marginBottom = '20px';
      } else if (id.value == '') {
        warn[0].innerHTML = '아이디를 입력하세요.';
        warn[0].style.marginBottom = '20px';
      } else {
        warn[0].innerHTML = '';
        warn[0].style.marginBottom = '0px';
        isCorrect++;
      }

      if (!passwordReg.test(password.value)) {
        warn[1].innerHTML = '형식이 맞지 않습니다.';
        warn[1].style.marginBottom = '20px';
      } else if (password.value == '') {
        warn[1].innerHTML = '비밀번호를 입력하세요.';
        warn[1].style.marginBottom = '20px';
      } else {
        warn[1].innerHTML = '';
        warn[1].style.marginBottom = '0';
        isCorrect++;
      }

      if (!nick_nameReg.test(nick_name.value)) {
        warn[2].innerHTML = '형식이 맞지 않습니다.';
        warn[2].style.marginBottom = '20px';
      } else if (nick_name.value == '') {
        warn[2].innerHTML = '닉네임을 입력하세요.';
        warn[2].style.marginBottom = '20px';
      } else {
        warn[2].innerHTML = '';
        warn[2].style.marginBottom = '0px';
        isCorrect++;
      }

      if (name.value == '') {
        warn[3].innerHTML = '이름을 입력하세요';
        warn[3].style.marginBottom = '20px';
      } else if (!nameReg.test(name.value)) {
        warn[3].innerHTML = '형식이 맞지 않습니다.';
        warn[3].style.marginBottom = '20px';
      } else {
        warn[3].innerHTML = '';
        warn[3].style.marginBottom = '0';
        isCorrect++;
      }
      if (email.value == '') {
        warn[4].innerHTML = '이메일을 입력하세요.';
        warn[4].style.marginBottom = '20px';
      } else if (!emailReg.test(email.value)) {
        warn[4].innerHTML = '형식이 맞지 않습니다.';
        warn[4].style.marginBottom = '20px';
      } else {
        warn[4].innerHTML = '';
        warn[4].style.marginBottom = '0';
        isCorrect++;
      }

      if (!idIsTrue) {
        alert('아이디 중복 체크를 해주세요.');
      }else if(!nickNameIsTrue){
        alert('닉네임 중복 체크를 해주세요.');
      }else {
        console.log(isCorrect);
        if(isCorrect==5){
            let selectedGender;
            let url = '/member/joinProc';

            for (const radio of genderRadios) {
              if (radio.checked) {
                selectedGender = radio.value;
                break;
              }
            }
            const member = new Member(
              id.value,
              password.value,
              nick_name.value,
              name.value,
              selectedGender,
              email.value
            );

            let result = await postRequest(url, member);

            if (result == 1) {
                let result1 = await('/member/profilePictureSetting');
                alert('회원가입 성공!');
                location.href = '/';
            }
        }
      }
    }