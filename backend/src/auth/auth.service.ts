import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AuthService {
  constructor(private prisma: PrismaService) {}

  async register(registerDto: any) {
    const { email, password, fullName } = registerDto;
    
    // In production, hash password
    const user = await this.prisma.user.create({
      data: {
        email,
        passwordHash: password, // TODO: bcrypt hash
      },
    });

    const applicant = await this.prisma.applicant.create({
      data: {
        userId: user.id,
        fullName: fullName,
      },
    });

    return { message: 'Registration successful', applicantId: applicant.id };
  }

  async login(loginDto: any) {
    const { email, password } = loginDto;
    
    const user = await this.prisma.user.findUnique({
      where: { email },
      include: { applicantProfile: true },
    });

    if (!user || user.passwordHash !== password) {
      throw new UnauthorizedException('Invalid credentials');
    }

    // In production, generate JWT token
    return { 
      message: 'Login successful', 
      token: 'fake-jwt-token-for-hackathon-demo',
      applicantId: user.applicantProfile?.id
    };
  }

  async logout() {
    // In production, invalidate token/session
    return { message: 'Logged out successfully' };
  }
}
