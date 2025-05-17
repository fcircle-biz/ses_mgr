package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring SecurityのUserDetailsServiceインターフェースの実装クラス。
 * ユーザー認証に必要な情報を提供します。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * ユーザー名（メールアドレス）でユーザーを検索し、Spring Security用のUserDetailsオブジェクトを返します
     *
     * @param username ユーザー名（メールアドレス）
     * @return UserDetailsオブジェクト
     * @throws UsernameNotFoundException ユーザーが見つからない場合
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // メールアドレスでユーザーを検索
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("メールアドレス " + username + " のユーザーが見つかりませんでした"));

        return buildUserDetails(user);
    }

    /**
     * 内部メソッド: User エンティティから Spring Security の UserDetails オブジェクトを構築します
     *
     * @param user ユーザーエンティティ
     * @return UserDetailsオブジェクト
     */
    private UserDetails buildUserDetails(User user) {
        // ユーザーの権限（ロールと権限）を取得
        Collection<GrantedAuthority> authorities = getAuthorities(user);

        // Spring Security用のUserDetailsオブジェクトを作成して返す
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                authorities
        );
    }

    /**
     * ユーザーのロールと権限からGrantedAuthorityのコレクションを取得します
     *
     * @param user ユーザーエンティティ
     * @return GrantedAuthorityのコレクション
     */
    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // ユーザーのロールを取得
        Optional<Role> roleOpt = roleRepository.findById(user.getRoleId());
        
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            
            // ロール自体をGrantedAuthorityとして追加 (ROLE_プレフィックスを付ける)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
            
            // ロールに関連付けられた権限も追加
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        
        return authorities;
    }
}