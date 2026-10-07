import { AuthService } from './auth.service';
export declare class AuthController {
    private readonly authService;
    constructor(authService: AuthService);
    register(registerDto: any): Promise<{
        message: string;
        applicantId: string;
    }>;
    login(loginDto: any): Promise<{
        message: string;
        token: string;
        applicantId: string | undefined;
    }>;
    logout(): Promise<{
        message: string;
    }>;
}
