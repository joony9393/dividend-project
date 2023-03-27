//package com.dayone.service;
//
//import com.dayone.model.Auth;
//import com.dayone.persist.entity.MemberEntity;
//import com.dayone.persist.MemberRepository;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//@Slf4j
//@Service
//@AllArgsConstructor
//public class MemberService implements UserDetailsService {
//
//    private final PasswordEncoder passwordEncoder;
//    private final MemberRepository memberRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return this.memberRepository.findByUsername(username)
//            .orElseThrow(() -> new UsernameNotFoundException("사용자 " + username + "을(를) 찾을 수 없습니다."));
//    }
//
//    // 회원 가입
//    public MemberEntity register(Auth.SignUp member){
//
//        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
//        if(exists){
//            throw new RuntimeException("이미 사용중인 아이디입니다.");
//        }
//        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
//        var result = this.memberRepository.save(member.toEntity());
//        return result;
//    }
//
//    // 로그인 검증 메서드
//    public MemberEntity authenticate(Auth.SignIn member){
//
//        // user의 비번은 인코딩된 상태이다.member의 비번은 암호화되지 않은 상태
//         var user = this.memberRepository.findByUsername(member.getUsername())
//                                    .orElseThrow(() -> new RuntimeException("존재하지 않는 ID입니다."));
//         // 비번 확인
//        if(!this.passwordEncoder.matches(member.getPassword(), user.getPassword())){
//            throw new RuntimeException("비밀 번호가 일치하지 않습니다.");
//        }
//        return user;
//    }
//}
package com.dayone.service;
import com.dayone.exception.impl.AlreadyExistUserException;
import com.dayone.model.Auth;
import com.dayone.persist.MemberRepository;
import com.dayone.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // 실제 구현체는 Appconfig

    @Override //스프링 시큐리티를 사용하기 위해 필요
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if(exists) {
            throw new AlreadyExistUserException();  // 이미 존재하는 경우 추가하기
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());

        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member) {

        var user = this.memberRepository.findByUsername(member.getUsername())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다"));

        if(!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }
        return user;
    }
}

