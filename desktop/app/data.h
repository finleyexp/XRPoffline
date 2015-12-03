#pragma once

#include <stdint.h>

class AEScrypter;

class AccountData
{
public:	
	int Init(const QString& _name, const QString& _addr, const QString& _key, int _sequence);
    void SetName(const QString& _name) { name=_name; }
	const QString& GetName() const { return name; }
	const QString& GetAddr() const { return addr; }
	const QString& GetKey() const { return key; }
	int GetSequence() const { return sequence; }
	void SetSequence(int seq) { sequence = seq; }

	int GetId() { return itemId; }

    bool CanSend() { return canSend; }
    bool CanReceive() {return canReceive; }

	bool LoadFromJson(QJsonObject &json, AEScrypter* crypter);
	void SaveToJson(QJsonObject &json, AEScrypter* crypter);
		
private:
	QString name;
	QString addr;
	QString key;
	int sequence;
	int itemId;
    bool canSend; //valid addr & key pair?
    bool canReceive; //valid addr ?

	static int itemIdCounter;
};

class Wallet
{
public:
	static Wallet* GetWallet();

    void SetNewPassword(const QString& password);
	std::vector<int> LoadFromJson(const QString& jsonString, const QString& password);
    QString SaveAccounts();

	int AddAccount(AccountData& d);
	void DeleteAccount(int accountId);

	AccountData* GetAccount(int accountId);

private:
	Wallet();
	~Wallet();

	std::vector<AccountData> accounts;
	std::string saltHex;
	std::string password;
    AEScrypter* crypter;

	static Wallet* wallet;
};
