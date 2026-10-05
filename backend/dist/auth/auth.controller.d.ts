import { AuthService } from './auth.service';
export declare class AuthController {
    private readonly authService;
    constructor(authService: AuthService);
    register(registerDto: any): Promise<any>;
    login(loginDto: any): Promise<any>;
    logout(): Promise<any>;
}
